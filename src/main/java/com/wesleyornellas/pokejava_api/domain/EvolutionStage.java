package com.wesleyornellas.pokejava_api.domain;

import java.util.List;

public record EvolutionStage(String name, List<EvolutionStage> evolvesTo) {

	public EvolutionStage {
		evolvesTo = List.copyOf(evolvesTo);
	}

}
