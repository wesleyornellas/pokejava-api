package com.wesleyornellas.pokejava_api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.domain.EvolutionStage;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;

class LocalPokemonServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-19T12:00:00Z");
	private static final PokemonDetail BULBASAUR_DETAIL = new PokemonDetail(
			1,
			"bulbasaur",
			"https://img/1.png",
			"Seed Pokémon",
			"A strange seed was planted on its back at birth.",
			6.9,
			List.of("overgrow", "chlorophyll"),
			List.of(new PokemonStat("hp", 45)),
			new EvolutionStage("bulbasaur", List.of()));

	private final PokemonCatalog catalog = mock(PokemonCatalog.class);
	private final PokemonStore store = mock(PokemonStore.class);
	private final LocalPokemonService service =
			new LocalPokemonService(catalog, store, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void syncCreatesLocalCopyFromCatalog() {
		when(catalog.findDetail("bulbasaur")).thenReturn(BULBASAUR_DETAIL);
		when(store.findById(1)).thenReturn(Optional.empty());
		when(store.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		LocalPokemon saved = service.sync("Bulbasaur");

		assertThat(saved.id()).isEqualTo(1);
		assertThat(saved.name()).isEqualTo("bulbasaur");
		assertThat(saved.localName()).isNull();
		assertThat(saved.region()).isNull();
		assertThat(saved.tags()).isEmpty();
		assertThat(saved.syncedAt()).isEqualTo(NOW);
		verify(store).save(saved);
	}

	@Test
	void syncPreservesProprietaryFieldsOnRefresh() {
		LocalPokemon existing = new LocalPokemon(
				1,
				"bulbasaur",
				"https://old.png",
				"Old category",
				"Old description",
				5.0,
				List.of("old"),
				List.of(new PokemonStat("hp", 1)),
				"Bulbasaurzinho",
				"Kanto-South",
				List.of("starter", "grass"),
				Instant.parse("2020-01-01T00:00:00Z"));
		PokemonDetail refreshed = new PokemonDetail(
				1,
				"bulbasaur",
				"https://img/1.png",
				"Seed Pokémon",
				"Updated description",
				6.9,
				List.of("overgrow"),
				List.of(new PokemonStat("hp", 45)),
				new EvolutionStage("bulbasaur", List.of()));

		when(catalog.findDetail("1")).thenReturn(refreshed);
		when(store.findById(1)).thenReturn(Optional.of(existing));
		when(store.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		LocalPokemon saved = service.sync("1");

		assertThat(saved.imageUrl()).isEqualTo("https://img/1.png");
		assertThat(saved.description()).isEqualTo("Updated description");
		assertThat(saved.weightKg()).isEqualTo(6.9);
		assertThat(saved.localName()).isEqualTo("Bulbasaurzinho");
		assertThat(saved.region()).isEqualTo("Kanto-South");
		assertThat(saved.tags()).containsExactly("starter", "grass");
		assertThat(saved.syncedAt()).isEqualTo(NOW);

		ArgumentCaptor<LocalPokemon> captor = ArgumentCaptor.forClass(LocalPokemon.class);
		verify(store).save(captor.capture());
		assertThat(captor.getValue().localName()).isEqualTo("Bulbasaurzinho");
	}

	@Test
	void listsLocalPokemonPage() {
		LocalPokemon local = LocalPokemon.fromCatalog(BULBASAUR_DETAIL, NOW);
		when(store.findPage(0, 20)).thenReturn(List.of(local));
		when(store.count()).thenReturn(1L);

		PageResult<LocalPokemon> page = service.list(0, 20);

		assertThat(page.content()).containsExactly(local);
		assertThat(page.totalElements()).isEqualTo(1);
	}

	@Test
	void getsLocalPokemonByName() {
		LocalPokemon local = LocalPokemon.fromCatalog(BULBASAUR_DETAIL, NOW);
		when(store.findByName("bulbasaur")).thenReturn(Optional.of(local));

		assertThat(service.get("Bulbasaur")).isEqualTo(local);
		verifyNoInteractions(catalog);
	}

	@Test
	void getsLocalPokemonById() {
		LocalPokemon local = LocalPokemon.fromCatalog(BULBASAUR_DETAIL, NOW);
		when(store.findById(1)).thenReturn(Optional.of(local));

		assertThat(service.get("1")).isEqualTo(local);
	}

	@Test
	void rejectsMissingLocalPokemon() {
		when(store.findByName("missingno")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get("missingno")).isInstanceOf(PokemonNotFoundException.class);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, 101 })
	void rejectsInvalidLocalPageSize(int size) {
		assertThatThrownBy(() -> service.list(0, size)).isInstanceOf(InvalidPageRequestException.class);
		verifyNoInteractions(store);
	}

}
