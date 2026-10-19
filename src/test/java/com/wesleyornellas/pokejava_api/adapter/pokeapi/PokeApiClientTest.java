package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;

import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog.CatalogPage;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

@RestClientTest(components = PokeApiClient.class, properties = "pokeapi.base-url=https://pokeapi.test/api/v2")
class PokeApiClientTest {

	private static final String BASE_URL = "https://pokeapi.test/api/v2";

	@Autowired
	private PokeApiClient client;

	@Autowired
	private MockRestServiceServer server;

	@Test
	void listsPokemonNamesWithTotalCount() {
		server.expect(requestTo(BASE_URL + "/pokemon?offset=20&limit=2"))
				.andRespond(withSuccess(fixture("pokemon-page.json"), APPLICATION_JSON));

		CatalogPage page = client.listPokemon(20, 2);

		assertThat(page.names()).containsExactly("spearow", "fearow");
		assertThat(page.totalCount()).isEqualTo(1351);
	}

	@Test
	void buildsSummaryFromPokemonAndSpeciesResources() {
		server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
				.andRespond(withSuccess(fixture("pokemon-bulbasaur.json"), APPLICATION_JSON));
		server.expect(requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
				.andRespond(withSuccess(fixture("species-bulbasaur.json"), APPLICATION_JSON));

		PokemonSummary summary = client.findSummary("bulbasaur");

		assertThat(summary.id()).isEqualTo(1);
		assertThat(summary.name()).isEqualTo("bulbasaur");
		assertThat(summary.spriteUrl())
				.isEqualTo("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png");
		assertThat(summary.category()).isEqualTo("Seed Pokémon");
		assertThat(summary.weightKg()).isEqualTo(6.9);
		assertThat(summary.abilities()).containsExactly("overgrow", "chlorophyll");
	}

	@Test
	void reportsCatalogUnavailableWhenPokeApiFails() {
		server.expect(requestTo(BASE_URL + "/pokemon?offset=0&limit=20")).andRespond(withServiceUnavailable());

		assertThatThrownBy(() -> client.listPokemon(0, 20)).isInstanceOf(CatalogUnavailableException.class);
	}

	@Test
	void reportsCatalogUnavailableWhenPokeApiTimesOut() {
		server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur")).andRespond(withException(new SocketTimeoutException()));

		assertThatThrownBy(() -> client.findSummary("bulbasaur")).isInstanceOf(CatalogUnavailableException.class);
	}

	@Test
	void buildsDetailWithStatsDescriptionAndEvolutionChain() {
		server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
				.andRespond(withSuccess(fixture("pokemon-bulbasaur.json"), APPLICATION_JSON));
		server.expect(requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
				.andRespond(withSuccess(fixture("species-bulbasaur.json"), APPLICATION_JSON));
		server.expect(requestTo(BASE_URL + "/evolution-chain/1/"))
				.andRespond(withSuccess(fixture("evolution-chain-1.json"), APPLICATION_JSON));

		PokemonDetail detail = client.findDetail("bulbasaur");

		assertThat(detail.id()).isEqualTo(1);
		assertThat(detail.name()).isEqualTo("bulbasaur");
		assertThat(detail.imageUrl()).isEqualTo(
				"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png");
		assertThat(detail.category()).isEqualTo("Seed Pokémon");
		assertThat(detail.description()).isEqualTo(
				"A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.");
		assertThat(detail.weightKg()).isEqualTo(6.9);
		assertThat(detail.abilities()).containsExactly("overgrow", "chlorophyll");
		assertThat(detail.stats()).extracting(PokemonStat::name, PokemonStat::baseStat)
				.containsExactly(
						tuple("hp", 45),
						tuple("attack", 49),
						tuple("defense", 49),
						tuple("special-attack", 65),
						tuple("special-defense", 65),
						tuple("speed", 45));
		assertThat(detail.evolutionChain().name()).isEqualTo("bulbasaur");
		assertThat(detail.evolutionChain().evolvesTo()).hasSize(1);
		assertThat(detail.evolutionChain().evolvesTo().getFirst().name()).isEqualTo("ivysaur");
		assertThat(detail.evolutionChain().evolvesTo().getFirst().evolvesTo().getFirst().name()).isEqualTo("venusaur");
	}

	@Test
	void reportsNotFoundWhenPokemonIsMissing() {
		server.expect(requestTo(BASE_URL + "/pokemon/missingno")).andRespond(withResourceNotFound());

		assertThatThrownBy(() -> client.findDetail("missingno")).isInstanceOf(PokemonNotFoundException.class);
	}

	private static ClassPathResource fixture(String name) {
		return new ClassPathResource("pokeapi/" + name);
	}

}
