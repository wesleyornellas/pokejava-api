package com.wesleyornellas.pokejava_api.application;

import java.time.Clock;
import java.util.Optional;

import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;

public class LocalPokemonService {

	static final int MAX_PAGE_SIZE = 100;

	private final PokemonCatalog catalog;
	private final PokemonStore store;
	private final Clock clock;

	public LocalPokemonService(PokemonCatalog catalog, PokemonStore store, Clock clock) {
		this.catalog = catalog;
		this.store = store;
		this.clock = clock;
	}

	/**
	 * Pulls a Pokémon from the external catalog and upserts it into the local store.
	 * Proprietary fields ({@code localName}, {@code region}, {@code tags}) are preserved on re-sync.
	 */
	public LocalPokemon sync(String idOrName) {
		String key = normalize(idOrName);
		PokemonDetail detail = catalog.findDetail(key);
		LocalPokemon local = store.findById(detail.id())
				.map(existing -> existing.withCatalogData(detail, clock.instant()))
				.orElseGet(() -> LocalPokemon.fromCatalog(detail, clock.instant()));
		return store.save(local);
	}

	public PageResult<LocalPokemon> list(int page, int size) {
		if (page < 0) {
			throw new InvalidPageRequestException("page must be zero or greater");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new InvalidPageRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
		}
		return new PageResult<>(store.findPage(page * size, size), page, size, store.count());
	}

	public LocalPokemon get(String idOrName) {
		String key = normalize(idOrName);
		return findLocal(key).orElseThrow(() -> new PokemonNotFoundException(key));
	}

	private Optional<LocalPokemon> findLocal(String idOrName) {
		if (idOrName.chars().allMatch(Character::isDigit)) {
			return store.findById(Integer.parseInt(idOrName));
		}
		return store.findByName(idOrName);
	}

	private static String normalize(String idOrName) {
		if (idOrName == null || idOrName.isBlank()) {
			throw new PokemonNotFoundException(idOrName == null ? "" : idOrName);
		}
		return idOrName.trim().toLowerCase();
	}

}
