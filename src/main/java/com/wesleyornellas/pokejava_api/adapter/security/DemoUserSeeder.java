package com.wesleyornellas.pokejava_api.adapter.security;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.User;

/**
 * Ensures a demo account exists for local demos and interview walkthroughs.
 */
@Component
class DemoUserSeeder implements ApplicationRunner {

	static final String DEMO_USERNAME = "demo";
	static final String DEMO_PASSWORD = "demo1234";
	static final String DEMO_DISPLAY_NAME = "Demo Trainer";

	private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

	private final UserStore userStore;
	private final PasswordHasher passwordHasher;
	private final Clock clock;

	DemoUserSeeder(UserStore userStore, PasswordHasher passwordHasher, Clock clock) {
		this.userStore = userStore;
		this.passwordHasher = passwordHasher;
		this.clock = clock;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (userStore.existsByUsername(DEMO_USERNAME)) {
			return;
		}
		userStore.save(User.create(
				DEMO_USERNAME,
				passwordHasher.hash(DEMO_PASSWORD),
				DEMO_DISPLAY_NAME,
				clock.instant()));
		log.info("Seeded demo user '{}' (password documented in README)", DEMO_USERNAME);
	}

}
