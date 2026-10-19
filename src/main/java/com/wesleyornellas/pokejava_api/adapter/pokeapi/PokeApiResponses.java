package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The subset of PokeAPI's JSON that this application reads. Unknown fields are ignored.
 */
final class PokeApiResponses {

	private PokeApiResponses() {
	}

	record NamedResource(String name) {
	}

	record PageResponse(long count, List<NamedResource> results) {
	}

	record PokemonResponse(int id, String name, int weight, Sprites sprites, List<AbilitySlot> abilities,
			NamedResource species) {
	}

	record Sprites(@JsonProperty("front_default") String frontDefault) {
	}

	record AbilitySlot(NamedResource ability) {
	}

	record SpeciesResponse(List<Genus> genera) {
	}

	record Genus(String genus, NamedResource language) {
	}

}
