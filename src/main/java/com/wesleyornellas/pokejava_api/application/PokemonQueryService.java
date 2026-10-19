package com.wesleyornellas.pokejava_api.application;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog.CatalogPage;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

public class PokemonQueryService {

	static final int MAX_PAGE_SIZE = 100;

	private final PokemonCatalog catalog;

	public PokemonQueryService(PokemonCatalog catalog) {
		this.catalog = catalog;
	}

	public PageResult<PokemonSummary> listPokemon(int page, int size) {
		if (page < 0) {
			throw new InvalidPageRequestException("page must be zero or greater");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new InvalidPageRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
		}

		CatalogPage catalogPage = catalog.listPokemon((long) page * size, size);
		return new PageResult<>(findSummaries(catalogPage.names()), page, size, catalogPage.totalCount());
	}

	public PokemonDetail getPokemon(String idOrName) {
		if (idOrName == null || idOrName.isBlank()) {
			throw new PokemonNotFoundException(idOrName == null ? "" : idOrName);
		}
		return catalog.findDetail(idOrName.trim().toLowerCase());
	}

	// The catalog's list endpoint only returns names, so each summary is a separate
	// remote lookup. Running them on virtual threads keeps a page as slow as its slowest entry.
	private List<PokemonSummary> findSummaries(List<String> names) {
		List<Future<PokemonSummary>> lookups;
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			lookups = names.stream()
					.map(name -> executor.submit(() -> catalog.findSummary(name)))
					.toList();
		}
		return lookups.stream().map(PokemonQueryService::resultOf).toList();
	}

	private static PokemonSummary resultOf(Future<PokemonSummary> lookup) {
		if (lookup.state() == Future.State.FAILED && lookup.exceptionNow() instanceof RuntimeException e) {
			throw e;
		}
		return lookup.resultNow();
	}

}
