package com.wesleyornellas.pokejava_api.application.port;

import java.util.List;
import java.util.Optional;

import com.wesleyornellas.pokejava_api.domain.LocalPokemon;

/**
 * Local persistence for replicated Pokémon.
 */
public interface PokemonStore {

	LocalPokemon save(LocalPokemon pokemon);

	Optional<LocalPokemon> findById(int id);

	Optional<LocalPokemon> findByName(String name);

	List<LocalPokemon> findPage(int offset, int limit);

	long count();

}
