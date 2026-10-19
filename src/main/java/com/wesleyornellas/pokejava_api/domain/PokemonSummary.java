package com.wesleyornellas.pokejava_api.domain;

import java.util.List;

public record PokemonSummary(
		int id,
		String name,
		String spriteUrl,
		String category,
		double weightKg,
		List<String> abilities) {

	public PokemonSummary {
		abilities = List.copyOf(abilities);
	}

}
