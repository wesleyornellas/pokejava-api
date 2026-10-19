package com.wesleyornellas.pokejava_api.domain;

import java.time.Instant;
import java.util.List;

/**
 * Locally replicated Pokémon with optional proprietary enrichment fields.
 */
public record LocalPokemon(
		int id,
		String name,
		String imageUrl,
		String category,
		String description,
		double weightKg,
		List<String> abilities,
		List<PokemonStat> stats,
		String localName,
		String region,
		List<String> tags,
		Instant syncedAt) {

	public LocalPokemon {
		abilities = List.copyOf(abilities);
		stats = List.copyOf(stats);
		tags = tags == null ? List.of() : List.copyOf(tags);
	}

	public static LocalPokemon fromCatalog(PokemonDetail detail, Instant syncedAt) {
		return new LocalPokemon(
				detail.id(),
				detail.name(),
				detail.imageUrl(),
				detail.category(),
				detail.description(),
				detail.weightKg(),
				detail.abilities(),
				detail.stats(),
				null,
				null,
				List.of(),
				syncedAt);
	}

	/**
	 * Refreshes catalog-sourced fields while keeping proprietary enrichment intact.
	 */
	public LocalPokemon withCatalogData(PokemonDetail detail, Instant syncedAt) {
		return new LocalPokemon(
				detail.id(),
				detail.name(),
				detail.imageUrl(),
				detail.category(),
				detail.description(),
				detail.weightKg(),
				detail.abilities(),
				detail.stats(),
				localName,
				region,
				tags,
				syncedAt);
	}

	/**
	 * Replaces proprietary enrichment fields; catalog-sourced data stays unchanged.
	 */
	public LocalPokemon withEnrichment(PokemonEnrichment enrichment) {
		return new LocalPokemon(
				id,
				name,
				imageUrl,
				category,
				description,
				weightKg,
				abilities,
				stats,
				enrichment.localName(),
				enrichment.region(),
				enrichment.tags(),
				syncedAt);
	}

}
