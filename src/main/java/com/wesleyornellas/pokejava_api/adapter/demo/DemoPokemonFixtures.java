package com.wesleyornellas.pokejava_api.adapter.demo;

import java.time.Instant;
import java.util.List;

import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;

/**
 * Deterministic local Pokémon used for interview demos — no PokeAPI call required.
 */
final class DemoPokemonFixtures {

	private DemoPokemonFixtures() {
	}

	static List<LocalPokemon> all(Instant syncedAt) {
		return List.of(
				pokemon(
						1,
						"bulbasaur",
						"Seed Pokémon",
						"A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.",
						6.9,
						List.of("overgrow", "chlorophyll"),
						List.of(stat("hp", 45), stat("attack", 49), stat("defense", 49), stat("speed", 45)),
						"Bulbasaurzinho",
						"Kanto-South",
						List.of("starter", "grass", "demo"),
						syncedAt),
				pokemon(
						4,
						"charmander",
						"Lizard Pokémon",
						"Obviously prefers hot places. When it rains, steam is said to spout from the tip of its tail.",
						8.5,
						List.of("blaze", "solar-power"),
						List.of(stat("hp", 39), stat("attack", 52), stat("defense", 43), stat("speed", 65)),
						"Charmandinho",
						"Kanto-Central",
						List.of("starter", "fire", "demo"),
						syncedAt),
				pokemon(
						7,
						"squirtle",
						"Tiny Turtle Pokémon",
						"After birth, its back swells and hardens into a shell. It powerfully sprays foam from its mouth.",
						9.0,
						List.of("torrent", "rain-dish"),
						List.of(stat("hp", 44), stat("attack", 48), stat("defense", 65), stat("speed", 43)),
						"Squirtlezinho",
						"Kanto-Coast",
						List.of("starter", "water", "demo"),
						syncedAt),
				pokemon(
						25,
						"pikachu",
						"Mouse Pokémon",
						"When several of these Pokémon gather, their electricity can cause lightning storms.",
						6.0,
						List.of("static", "lightning-rod"),
						List.of(stat("hp", 35), stat("attack", 55), stat("defense", 40), stat("speed", 90)),
						"Pikachuzinho",
						"Kanto-Power",
						List.of("mascot", "electric", "demo"),
						syncedAt));
	}

	private static LocalPokemon pokemon(
			int id,
			String name,
			String category,
			String description,
			double weightKg,
			List<String> abilities,
			List<PokemonStat> stats,
			String localName,
			String region,
			List<String> tags,
			Instant syncedAt) {
		return new LocalPokemon(
				id,
				name,
				"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
						+ id + ".png",
				category,
				description,
				weightKg,
				abilities,
				stats,
				localName,
				region,
				tags,
				syncedAt);
	}

	private static PokemonStat stat(String name, int baseStat) {
		return new PokemonStat(name, baseStat);
	}

}
