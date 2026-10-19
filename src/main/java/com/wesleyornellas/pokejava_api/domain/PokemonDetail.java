package com.wesleyornellas.pokejava_api.domain;

import java.util.List;

public record PokemonDetail(
		int id,
		String name,
		String imageUrl,
		String category,
		String description,
		double weightKg,
		List<String> abilities,
		List<PokemonStat> stats,
		EvolutionStage evolutionChain) {

	public PokemonDetail {
		abilities = List.copyOf(abilities);
		stats = List.copyOf(stats);
	}

}
