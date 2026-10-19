package com.wesleyornellas.pokejava_api.adapter.pokeapi;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.wesleyornellas.pokejava_api.application.port.PokemonCatalog;

@Configuration
class PokeApiCacheConfig {

	@Bean
	@Primary
	PokemonCatalog cachingPokemonCatalog(
			PokeApiClient pokeApiClient,
			@Value("${pokeapi.cache.maximum-size:500}") long maximumSize,
			@Value("${pokeapi.cache.expire-after-write:10m}") Duration expireAfterWrite) {
		return new CachingPokemonCatalog(pokeApiClient, maximumSize, expireAfterWrite);
	}

}
