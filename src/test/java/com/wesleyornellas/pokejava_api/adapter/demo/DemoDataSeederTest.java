package com.wesleyornellas.pokejava_api.adapter.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.User;

class DemoDataSeederTest {

	private static final Instant NOW = Instant.parse("2026-10-19T18:00:00Z");

	private final UserStore userStore = mock(UserStore.class);
	private final PokemonStore pokemonStore = mock(PokemonStore.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private DemoDataSeeder seeder;

	@BeforeEach
	void setUp() {
		seeder = new DemoDataSeeder(
				new DemoProperties(true, "demo", "demo1234", "Demo Trainer"),
				userStore,
				pokemonStore,
				passwordHasher,
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void seedsDemoUserAndPokemonWhenStoreIsEmpty() {
		when(userStore.existsByUsername("demo")).thenReturn(false);
		when(passwordHasher.hash("demo1234")).thenReturn("hashed");
		when(pokemonStore.count()).thenReturn(0L);

		seeder.run(new DefaultApplicationArguments());

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userStore).save(userCaptor.capture());
		assertThat(userCaptor.getValue().username()).isEqualTo("demo");
		assertThat(userCaptor.getValue().passwordHash()).isEqualTo("hashed");
		assertThat(userCaptor.getValue().displayName()).isEqualTo("Demo Trainer");

		ArgumentCaptor<LocalPokemon> pokemonCaptor = ArgumentCaptor.forClass(LocalPokemon.class);
		verify(pokemonStore, times(DemoPokemonFixtures.all(NOW).size())).save(pokemonCaptor.capture());
		assertThat(pokemonCaptor.getAllValues())
				.extracting(LocalPokemon::name)
				.containsExactly("bulbasaur", "charmander", "squirtle", "pikachu");
		assertThat(pokemonCaptor.getAllValues())
				.allSatisfy(pokemon -> {
					assertThat(pokemon.localName()).isNotBlank();
					assertThat(pokemon.region()).isNotBlank();
					assertThat(pokemon.tags()).isNotEmpty();
					assertThat(pokemon.syncedAt()).isEqualTo(NOW);
				});
	}

	@Test
	void skipsSeedingWhenDisabled() {
		seeder = new DemoDataSeeder(
				new DemoProperties(false, "demo", "demo1234", "Demo Trainer"),
				userStore,
				pokemonStore,
				passwordHasher,
				Clock.fixed(NOW, ZoneOffset.UTC));

		seeder.run(new DefaultApplicationArguments());

		verify(userStore, never()).save(any());
		verify(pokemonStore, never()).save(any());
	}

	@Test
	void doesNotDuplicateExistingDemoData() {
		when(userStore.existsByUsername("demo")).thenReturn(true);
		when(pokemonStore.count()).thenReturn(4L);

		seeder.run(new DefaultApplicationArguments());

		verify(userStore, never()).save(any());
		verify(pokemonStore, never()).save(any());
	}

}
