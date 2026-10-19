package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog.CatalogPage;
import com.wesleyornellas.pokejava_api.domain.EvolutionStage;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

class CachingPokemonCatalogTest {

	private final PokemonCatalog delegate = mock(PokemonCatalog.class);
	private CachingPokemonCatalog catalog;

	@BeforeEach
	void setUp() {
		catalog = new CachingPokemonCatalog(delegate, 100, Duration.ofMinutes(10));
	}

	@Test
	void cachesPokemonDetailsByNormalizedKey() {
		PokemonDetail detail = detail("bulbasaur");
		when(delegate.findDetail("bulbasaur")).thenReturn(detail);

		assertThat(catalog.findDetail("Bulbasaur")).isSameAs(detail);
		assertThat(catalog.findDetail("bulbasaur")).isSameAs(detail);

		verify(delegate, times(1)).findDetail("bulbasaur");
	}

	@Test
	void cachesSummariesByNormalizedName() {
		PokemonSummary summary = new PokemonSummary(1, "bulbasaur", "https://img/1.png", "Seed", 6.9, List.of("overgrow"));
		when(delegate.findSummary("bulbasaur")).thenReturn(summary);

		assertThat(catalog.findSummary("BULBASAUR")).isSameAs(summary);
		assertThat(catalog.findSummary("bulbasaur")).isSameAs(summary);

		verify(delegate, times(1)).findSummary("bulbasaur");
	}

	@Test
	void cachesListPagesByOffsetAndLimit() {
		CatalogPage page = new CatalogPage(List.of("bulbasaur", "ivysaur"), 1351);
		when(delegate.listPokemon(0, 20)).thenReturn(page);

		assertThat(catalog.listPokemon(0, 20)).isSameAs(page);
		assertThat(catalog.listPokemon(0, 20)).isSameAs(page);

		verify(delegate, times(1)).listPokemon(0, 20);
	}

	private static PokemonDetail detail(String name) {
		return new PokemonDetail(
				1,
				name,
				"https://img/1.png",
				"Seed Pokémon",
				"Description",
				6.9,
				List.of("overgrow"),
				List.of(new PokemonStat("hp", 45)),
				new EvolutionStage(name, List.of()));
	}

}
