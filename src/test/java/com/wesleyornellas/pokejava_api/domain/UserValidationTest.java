package com.wesleyornellas.pokejava_api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidationTest {

	@Test
	void normalizesUsernameAndDisplayName() {
		User user = User.create("Ash.Ketchum", "hash", "  Ash  ", Instant.parse("2026-10-19T12:00:00Z"));

		assertThat(user.username()).isEqualTo("ash.ketchum");
		assertThat(user.displayName()).isEqualTo("Ash");
		assertThat(user.id()).isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "Ash!", "user name", "" })
	void rejectsInvalidUsernames(String username) {
		assertThatThrownBy(() -> User.normalizeUsername(username))
				.isInstanceOf(InvalidUserRegistrationException.class);
	}

	@Test
	void rejectsBlankPassword() {
		assertThatThrownBy(() -> User.requirePassword("   "))
				.isInstanceOf(InvalidUserRegistrationException.class);
	}

	@Test
	void constructsUserWithRequiredFields() {
		UUID id = UUID.randomUUID();
		Instant now = Instant.parse("2026-10-19T12:00:00Z");

		User user = new User(id, "demo", "hash", "Demo", now);

		assertThat(user.id()).isEqualTo(id);
		assertThat(user.username()).isEqualTo("demo");
	}

}
