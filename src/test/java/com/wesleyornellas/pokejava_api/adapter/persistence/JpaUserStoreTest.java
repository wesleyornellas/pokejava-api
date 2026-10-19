package com.wesleyornellas.pokejava_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import com.wesleyornellas.pokejava_api.TestcontainersConfiguration;
import com.wesleyornellas.pokejava_api.domain.User;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ JpaUserStore.class, TestcontainersConfiguration.class })
class JpaUserStoreTest {

	@Autowired
	private JpaUserStore store;

	@Test
	void savesAndFindsUserByUsername() {
		User user = new User(
				UUID.fromString("22222222-2222-2222-2222-222222222222"),
				"misty",
				"$2a$10$hashed",
				"Misty",
				Instant.parse("2026-10-19T12:00:00Z"));

		User saved = store.save(user);

		assertThat(store.findByUsername("misty")).contains(saved);
		assertThat(store.existsByUsername("misty")).isTrue();
		assertThat(store.existsByUsername("ash")).isFalse();
		assertThat(saved.displayName()).isEqualTo("Misty");
		assertThat(saved.passwordHash()).isEqualTo("$2a$10$hashed");
	}

}
