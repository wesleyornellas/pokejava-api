package com.wesleyornellas.pokejava_api.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wesleyornellas.pokejava_api.application.PokemonQueryService;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;

@Configuration
class ApplicationConfig {

	@Bean
	PokemonQueryService pokemonQueryService(PokemonCatalog catalog) {
		return new PokemonQueryService(catalog);
	}

}
