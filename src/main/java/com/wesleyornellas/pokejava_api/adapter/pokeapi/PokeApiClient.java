package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.AbilitySlot;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.ChainLink;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.EvolutionChainResponse;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.FlavorTextEntry;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.NamedResource;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.PageResponse;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.PokemonResponse;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.SpeciesResponse;
import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.domain.EvolutionStage;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

@Component
class PokeApiClient implements PokemonCatalog {

	private static final String ENGLISH = "en";

	private final RestClient restClient;

	PokeApiClient(RestClient.Builder restClientBuilder, @Value("${pokeapi.base-url}") String baseUrl) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
	}

	@Override
	public CatalogPage listPokemon(long offset, int limit) {
		PageResponse page = get(PageResponse.class, "/pokemon?offset={offset}&limit={limit}", offset, limit);
		return new CatalogPage(page.results().stream().map(NamedResource::name).toList(), page.count());
	}

	@Override
	public PokemonSummary findSummary(String name) {
		PokemonResponse pokemon = get(PokemonResponse.class, "/pokemon/{name}", name);
		SpeciesResponse species = get(SpeciesResponse.class, "/pokemon-species/{name}", pokemon.species().name());

		return new PokemonSummary(
				pokemon.id(),
				pokemon.name(),
				pokemon.sprites().frontDefault(),
				englishGenus(species),
				hectogramsToKg(pokemon.weight()),
				abilitiesOf(pokemon));
	}

	@Override
	public PokemonDetail findDetail(String idOrName) {
		PokemonResponse pokemon = getPokemonOrNotFound(idOrName);
		SpeciesResponse species = get(SpeciesResponse.class, "/pokemon-species/{name}", pokemon.species().name());
		EvolutionChainResponse evolution = getByAbsoluteUrl(EvolutionChainResponse.class, species.evolutionChain().url());

		return new PokemonDetail(
				pokemon.id(),
				pokemon.name(),
				imageUrl(pokemon),
				englishGenus(species),
				englishDescription(species),
				hectogramsToKg(pokemon.weight()),
				abilitiesOf(pokemon),
				statsOf(pokemon),
				toEvolutionStage(evolution.chain()));
	}

	private PokemonResponse getPokemonOrNotFound(String idOrName) {
		try {
			return restClient.get().uri("/pokemon/{idOrName}", idOrName).retrieve().body(PokemonResponse.class);
		}
		catch (RestClientResponseException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new PokemonNotFoundException(idOrName);
			}
			throw new CatalogUnavailableException("PokeAPI request failed: /pokemon/" + idOrName, e);
		}
		catch (RestClientException e) {
			throw new CatalogUnavailableException("PokeAPI request failed: /pokemon/" + idOrName, e);
		}
	}

	private <T> T get(Class<T> type, String uri, Object... uriVariables) {
		try {
			return restClient.get().uri(uri, uriVariables).retrieve().body(type);
		}
		catch (RestClientException e) {
			throw new CatalogUnavailableException("PokeAPI request failed: " + uri, e);
		}
	}

	private <T> T getByAbsoluteUrl(Class<T> type, String url) {
		try {
			return restClient.get().uri(URI.create(url)).retrieve().body(type);
		}
		catch (RestClientException e) {
			throw new CatalogUnavailableException("PokeAPI request failed: " + url, e);
		}
	}

	private static List<String> abilitiesOf(PokemonResponse pokemon) {
		return pokemon.abilities().stream().map(AbilitySlot::ability).map(NamedResource::name).toList();
	}

	private static List<PokemonStat> statsOf(PokemonResponse pokemon) {
		if (pokemon.stats() == null) {
			return List.of();
		}
		return pokemon.stats().stream()
				.map(stat -> new PokemonStat(stat.stat().name(), stat.baseStat()))
				.toList();
	}

	private static String imageUrl(PokemonResponse pokemon) {
		if (pokemon.sprites().other() != null
				&& pokemon.sprites().other().officialArtwork() != null
				&& pokemon.sprites().other().officialArtwork().frontDefault() != null) {
			return pokemon.sprites().other().officialArtwork().frontDefault();
		}
		return pokemon.sprites().frontDefault();
	}

	private static String englishGenus(SpeciesResponse species) {
		return species.genera().stream()
				.filter(genus -> ENGLISH.equals(genus.language().name()))
				.map(PokeApiResponses.Genus::genus)
				.findFirst()
				.orElse(null);
	}

	private static String englishDescription(SpeciesResponse species) {
		if (species.flavorTextEntries() == null) {
			return null;
		}
		return species.flavorTextEntries().stream()
				.filter(entry -> ENGLISH.equals(entry.language().name()))
				.map(FlavorTextEntry::flavorText)
				.map(PokeApiClient::normalizeFlavorText)
				.findFirst()
				.orElse(null);
	}

	private static String normalizeFlavorText(String flavorText) {
		return flavorText.replace("\n", " ").replace("\f", " ").trim();
	}

	private static EvolutionStage toEvolutionStage(ChainLink link) {
		List<ChainLink> evolvesTo = link.evolvesTo() == null ? List.of() : link.evolvesTo();
		return new EvolutionStage(
				link.species().name(),
				evolvesTo.stream().map(PokeApiClient::toEvolutionStage).toList());
	}

	private static double hectogramsToKg(int hectograms) {
		return hectograms / 10.0;
	}

}
