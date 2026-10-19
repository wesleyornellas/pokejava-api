package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

/**
 * Caffeine-backed cache in front of the external PokeAPI catalog.
 */
class CachingPokemonCatalog implements PokemonCatalog {

	private final PokemonCatalog delegate;
	private final Cache<String, CatalogPage> pageCache;
	private final Cache<String, PokemonSummary> summaryCache;
	private final Cache<String, PokemonDetail> detailCache;

	CachingPokemonCatalog(PokemonCatalog delegate, long maximumSize, Duration expireAfterWrite) {
		this.delegate = delegate;
		this.pageCache = caffeine(maximumSize, expireAfterWrite);
		this.summaryCache = caffeine(maximumSize, expireAfterWrite);
		this.detailCache = caffeine(maximumSize, expireAfterWrite);
	}

	@Override
	public CatalogPage listPokemon(long offset, int limit) {
		String key = offset + ":" + limit;
		return pageCache.get(key, ignored -> delegate.listPokemon(offset, limit));
	}

	@Override
	public PokemonSummary findSummary(String name) {
		String key = normalize(name);
		return summaryCache.get(key, ignored -> delegate.findSummary(key));
	}

	@Override
	public PokemonDetail findDetail(String idOrName) {
		String key = normalize(idOrName);
		return detailCache.get(key, ignored -> delegate.findDetail(key));
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private static <V> Cache<String, V> caffeine(long maximumSize, Duration expireAfterWrite) {
		return Caffeine.newBuilder()
				.maximumSize(maximumSize)
				.expireAfterWrite(expireAfterWrite.toMillis(), TimeUnit.MILLISECONDS)
				.build();
	}

}
