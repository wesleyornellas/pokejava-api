package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The subset of PokeAPI's JSON that this application reads. Unknown fields are ignored.
 */
final class PokeApiResponses {

	private PokeApiResponses() {
	}

	record NamedResource(String name, String url) {

		NamedResource {
			if (url == null) {
				url = "";
			}
		}

	}

	record PageResponse(long count, List<NamedResource> results) {
	}

	record PokemonResponse(int id, String name, int weight, Sprites sprites, List<AbilitySlot> abilities,
			List<StatSlot> stats, NamedResource species) {
	}

	record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {
	}

	record OtherSprites(@JsonProperty("official-artwork") OfficialArtwork officialArtwork) {
	}

	record OfficialArtwork(@JsonProperty("front_default") String frontDefault) {
	}

	record AbilitySlot(NamedResource ability) {
	}

	record StatSlot(@JsonProperty("base_stat") int baseStat, NamedResource stat) {
	}

	record SpeciesResponse(List<Genus> genera, @JsonProperty("flavor_text_entries") List<FlavorTextEntry> flavorTextEntries,
			@JsonProperty("evolution_chain") ResourceLink evolutionChain) {
	}

	record Genus(String genus, NamedResource language) {
	}

	record FlavorTextEntry(@JsonProperty("flavor_text") String flavorText, NamedResource language) {
	}

	record ResourceLink(String url) {
	}

	record EvolutionChainResponse(ChainLink chain) {
	}

	record ChainLink(NamedResource species, @JsonProperty("evolves_to") List<ChainLink> evolvesTo) {
	}

}
