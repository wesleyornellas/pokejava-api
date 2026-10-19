package com.wesleyornellas.pokejava_api.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wesleyornellas.pokejava_api.application.LocalPokemonService;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PageResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/local/pokemon")
class LocalPokemonController {

	private final LocalPokemonService localPokemonService;

	LocalPokemonController(LocalPokemonService localPokemonService) {
		this.localPokemonService = localPokemonService;
	}

	@GetMapping
	PageResult<LocalPokemon> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return localPokemonService.list(page, size);
	}

	@GetMapping("/{idOrName}")
	LocalPokemon detail(@PathVariable String idOrName) {
		return localPokemonService.get(idOrName);
	}

	@PostMapping("/{idOrName}/sync")
	@ResponseStatus(HttpStatus.CREATED)
	LocalPokemon sync(@PathVariable String idOrName) {
		return localPokemonService.sync(idOrName);
	}

	@PutMapping("/{idOrName}")
	LocalPokemon update(@PathVariable String idOrName, @Valid @RequestBody UpdateLocalPokemonRequest request) {
		return localPokemonService.update(idOrName, request.localName(), request.region(), request.tags());
	}

}
