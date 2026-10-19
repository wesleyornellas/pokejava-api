package com.wesleyornellas.pokejava_api.adapter.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wesleyornellas.pokejava_api.application.AuthService;
import com.wesleyornellas.pokejava_api.application.LocalPokemonService;
import com.wesleyornellas.pokejava_api.application.PokemonQueryService;
import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.application.port.TokenIssuer;
import com.wesleyornellas.pokejava_api.application.port.UserStore;

@Configuration
class ApplicationConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	PokemonQueryService pokemonQueryService(PokemonCatalog catalog) {
		return new PokemonQueryService(catalog);
	}

	@Bean
	LocalPokemonService localPokemonService(PokemonCatalog catalog, PokemonStore store, Clock clock) {
		return new LocalPokemonService(catalog, store, clock);
	}

	@Bean
	AuthService authService(
			UserStore userStore,
			PasswordHasher passwordHasher,
			TokenIssuer tokenIssuer,
			Clock clock) {
		return new AuthService(userStore, passwordHasher, tokenIssuer, clock);
	}

}
