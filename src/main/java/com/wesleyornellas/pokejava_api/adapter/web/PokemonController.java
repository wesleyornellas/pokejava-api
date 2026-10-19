package com.wesleyornellas.pokejava_api.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wesleyornellas.pokejava_api.application.PokemonQueryService;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

@RestController
@RequestMapping("/api/v1/pokemon")
class PokemonController {

	private final PokemonQueryService pokemonQueryService;

	PokemonController(PokemonQueryService pokemonQueryService) {
		this.pokemonQueryService = pokemonQueryService;
	}

	@GetMapping
	PageResult<PokemonSummary> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return pokemonQueryService.listPokemon(page, size);
	}

	@GetMapping("/{idOrName}")
	PokemonDetail detail(@PathVariable String idOrName) {
		return pokemonQueryService.getPokemon(idOrName);
	}

}
