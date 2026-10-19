package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.AbilitySlot;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.NamedResource;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.PageResponse;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.PokemonResponse;
import com.wesleyornellas.pokejava_api.adapter.pokeapi.PokeApiResponses.SpeciesResponse;
import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
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
				pokemon.abilities().stream().map(AbilitySlot::ability).map(NamedResource::name).toList());
	}

	private <T> T get(Class<T> type, String uri, Object... uriVariables) {
		try {
			return restClient.get().uri(uri, uriVariables).retrieve().body(type);
		}
		catch (RestClientException e) {
			throw new CatalogUnavailableException("PokeAPI request failed: " + uri, e);
		}
	}

	private static String englishGenus(SpeciesResponse species) {
		return species.genera().stream()
				.filter(genus -> ENGLISH.equals(genus.language().name()))
				.map(PokeApiResponses.Genus::genus)
				.findFirst()
				.orElse(null);
	}

	private static double hectogramsToKg(int hectograms) {
		return hectograms / 10.0;
	}

}
