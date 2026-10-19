package com.wesleyornellas.pokejava_api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog.CatalogPage;
import com.wesleyornellas.pokejava_api.domain.EvolutionStage;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

class PokemonQueryServiceTest {

	private static final PokemonSummary BULBASAUR =
			new PokemonSummary(1, "bulbasaur", "https://img/1.png", "Seed Pokémon", 6.9, List.of("overgrow", "chlorophyll"));
	private static final PokemonSummary IVYSAUR =
			new PokemonSummary(2, "ivysaur", "https://img/2.png", "Seed Pokémon", 13.0, List.of("overgrow", "chlorophyll"));

	private final PokemonCatalog catalog = mock(PokemonCatalog.class);
	private final PokemonQueryService service = new PokemonQueryService(catalog);

	@Test
	void listsPokemonPageInCatalogOrder() {
		when(catalog.listPokemon(0, 2)).thenReturn(new CatalogPage(List.of("bulbasaur", "ivysaur"), 1025));
		when(catalog.findSummary("bulbasaur")).thenReturn(BULBASAUR);
		when(catalog.findSummary("ivysaur")).thenReturn(IVYSAUR);

		PageResult<PokemonSummary> result = service.listPokemon(0, 2);

		assertThat(result.content()).containsExactly(BULBASAUR, IVYSAUR);
		assertThat(result.page()).isZero();
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.totalElements()).isEqualTo(1025);
		assertThat(result.totalPages()).isEqualTo(513);
	}

	@Test
	void translatesPageNumberIntoCatalogOffset() {
		when(catalog.listPokemon(20, 10)).thenReturn(new CatalogPage(List.of(), 1025));

		PageResult<PokemonSummary> result = service.listPokemon(2, 10);

		assertThat(result.content()).isEmpty();
	}

	@Test
	void doesNotOverflowOffsetForVeryLargePageNumbers() {
		long offset = (long) Integer.MAX_VALUE * 100;
		when(catalog.listPokemon(offset, 100)).thenReturn(new CatalogPage(List.of(), 1025));

		assertThat(service.listPokemon(Integer.MAX_VALUE, 100).content()).isEmpty();
	}

	@Test
	void rejectsNegativePage() {
		assertThatThrownBy(() -> service.listPokemon(-1, 20))
				.isInstanceOf(InvalidPageRequestException.class)
				.hasMessageContaining("page");
		verifyNoInteractions(catalog);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -5, 101 })
	void rejectsPageSizeOutsideAllowedRange(int size) {
		assertThatThrownBy(() -> service.listPokemon(0, size))
				.isInstanceOf(InvalidPageRequestException.class)
				.hasMessageContaining("size");
		verifyNoInteractions(catalog);
	}

	@Test
	void propagatesCatalogFailureWhileLoadingSummaries() {
		when(catalog.listPokemon(0, 2)).thenReturn(new CatalogPage(List.of("bulbasaur", "ivysaur"), 1025));
		when(catalog.findSummary("bulbasaur")).thenReturn(BULBASAUR);
		when(catalog.findSummary("ivysaur")).thenThrow(new CatalogUnavailableException("timeout", null));

		assertThatThrownBy(() -> service.listPokemon(0, 2)).isInstanceOf(CatalogUnavailableException.class);
	}

	@Test
	void returnsDetailForNormalizedName() {
		PokemonDetail detail = new PokemonDetail(
				1,
				"bulbasaur",
				"https://img/1.png",
				"Seed Pokémon",
				"A strange seed was planted on its back at birth.",
				6.9,
				List.of("overgrow", "chlorophyll"),
				List.of(new PokemonStat("hp", 45)),
				new EvolutionStage("bulbasaur", List.of()));
		when(catalog.findDetail("bulbasaur")).thenReturn(detail);

		assertThat(service.getPokemon(" Bulbasaur ")).isEqualTo(detail);
	}

	@Test
	void rejectsBlankDetailLookup() {
		assertThatThrownBy(() -> service.getPokemon("  "))
				.isInstanceOf(PokemonNotFoundException.class);
		verifyNoInteractions(catalog);
	}

}
