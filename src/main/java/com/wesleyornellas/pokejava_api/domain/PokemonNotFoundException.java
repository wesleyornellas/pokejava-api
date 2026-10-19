package com.wesleyornellas.pokejava_api.domain;

public class PokemonNotFoundException extends RuntimeException {

	public PokemonNotFoundException(String idOrName) {
		super("Pokémon not found: " + idOrName);
	}

}
