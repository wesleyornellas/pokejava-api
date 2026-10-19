package com.wesleyornellas.pokejava_api.application.port;

import java.util.List;

import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

/**
 * External source of Pokémon data (PokeAPI in production).
 */
public interface PokemonCatalog {

	CatalogPage listPokemon(long offset, int limit);

	PokemonSummary findSummary(String name);

	PokemonDetail findDetail(String idOrName);

	record CatalogPage(List<String> names, long totalCount) {

		public CatalogPage {
			names = List.copyOf(names);
		}

	}

}
