package com.wesleyornellas.pokejava_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import com.wesleyornellas.pokejava_api.TestcontainersConfiguration;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ JpaPokemonStore.class, TestcontainersConfiguration.class })
class JpaPokemonStoreTest {

	@Autowired
	private JpaPokemonStore store;

	@Test
	void savesAndFindsPokemonWithProprietaryFields() {
		LocalPokemon pokemon = new LocalPokemon(
				1,
				"bulbasaur",
				"https://img/1.png",
				"Seed Pokémon",
				"A strange seed was planted on its back at birth.",
				6.9,
				List.of("overgrow", "chlorophyll"),
				List.of(new PokemonStat("hp", 45), new PokemonStat("attack", 49)),
				"Bulbasaurzinho",
				"Kanto",
				List.of("starter", "grass"),
				Instant.parse("2026-10-19T12:00:00Z"));

		LocalPokemon saved = store.save(pokemon);

		assertThat(store.findById(1)).contains(saved);
		assertThat(store.findByName("bulbasaur")).contains(saved);
		assertThat(saved.localName()).isEqualTo("Bulbasaurzinho");
		assertThat(saved.region()).isEqualTo("Kanto");
		assertThat(saved.tags()).containsExactly("starter", "grass");
		assertThat(saved.stats()).containsExactly(new PokemonStat("hp", 45), new PokemonStat("attack", 49));
		assertThat(store.count()).isEqualTo(1);
		assertThat(store.findPage(0, 10)).containsExactly(saved);
	}

	@Test
	void upsertKeepsSamePrimaryKey() {
		LocalPokemon first = new LocalPokemon(
				25,
				"pikachu",
				"https://img/25.png",
				"Mouse Pokémon",
				"When several of these Pokémon gather, their electricity can cause lightning storms.",
				6.0,
				List.of("static"),
				List.of(new PokemonStat("speed", 90)),
				null,
				null,
				List.of(),
				Instant.parse("2026-01-01T00:00:00Z"));
		store.save(first);

		LocalPokemon updated = new LocalPokemon(
				25,
				"pikachu",
				"https://img/25-new.png",
				"Mouse Pokémon",
				"Updated",
				6.0,
				List.of("static", "lightning-rod"),
				List.of(new PokemonStat("speed", 90)),
				"Pikachu local",
				"Vermilion",
				List.of("mascot"),
				Instant.parse("2026-10-19T12:00:00Z"));

		store.save(updated);

		assertThat(store.count()).isEqualTo(1);
		assertThat(store.findById(25)).contains(updated);
	}

}
