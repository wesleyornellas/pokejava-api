package com.wesleyornellas.pokejava_api.adapter.demo;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.User;

/**
 * Seeds mock credentials and a small local Pokémon catalog for zero-friction demos.
 */
@Component
class DemoDataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	private final DemoProperties properties;
	private final UserStore userStore;
	private final PokemonStore pokemonStore;
	private final PasswordHasher passwordHasher;
	private final Clock clock;

	DemoDataSeeder(
			DemoProperties properties,
			UserStore userStore,
			PokemonStore pokemonStore,
			PasswordHasher passwordHasher,
			Clock clock) {
		this.properties = properties;
		this.userStore = userStore;
		this.pokemonStore = pokemonStore;
		this.passwordHasher = passwordHasher;
		this.clock = clock;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.seedEnabled()) {
			log.info("Demo data seeding disabled (app.demo.seed-enabled=false)");
			return;
		}
		seedDemoUser();
		seedDemoPokemon();
	}

	private void seedDemoUser() {
		String username = properties.username().trim().toLowerCase();
		if (userStore.existsByUsername(username)) {
			return;
		}
		userStore.save(User.create(
				username,
				passwordHasher.hash(properties.password()),
				properties.displayName(),
				clock.instant()));
		log.info("Seeded demo user '{}' — see README for credentials", username);
	}

	private void seedDemoPokemon() {
		if (pokemonStore.count() > 0) {
			return;
		}
		var fixtures = DemoPokemonFixtures.all(clock.instant());
		for (LocalPokemon pokemon : fixtures) {
			pokemonStore.save(pokemon);
		}
		log.info("Seeded {} demo Pokémon into the local store", fixtures.size());
	}

}
