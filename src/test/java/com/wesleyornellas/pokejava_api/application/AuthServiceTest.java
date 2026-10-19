package com.wesleyornellas.pokejava_api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.wesleyornellas.pokejava_api.application.AuthService.AuthResult;
import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.TokenIssuer;
import com.wesleyornellas.pokejava_api.application.port.TokenIssuer.IssuedToken;
import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.DuplicateUsernameException;
import com.wesleyornellas.pokejava_api.domain.InvalidCredentialsException;
import com.wesleyornellas.pokejava_api.domain.InvalidUserRegistrationException;
import com.wesleyornellas.pokejava_api.domain.User;

class AuthServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-19T15:00:00Z");
	private static final Instant TOKEN_EXPIRY = Instant.parse("2026-10-19T16:00:00Z");

	private final UserStore userStore = mock(UserStore.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final TokenIssuer tokenIssuer = mock(TokenIssuer.class);
	private final AuthService service =
			new AuthService(userStore, passwordHasher, tokenIssuer, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void registersNewUserAndReturnsToken() {
		when(userStore.existsByUsername("ash")).thenReturn(false);
		when(passwordHasher.hash("pikachu12")).thenReturn("hashed");
		when(userStore.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(tokenIssuer.issue(any())).thenReturn(new IssuedToken("jwt-token", TOKEN_EXPIRY));

		AuthResult result = service.register("Ash", "pikachu12", " Ash Ketchum ");

		assertThat(result.username()).isEqualTo("ash");
		assertThat(result.displayName()).isEqualTo("Ash Ketchum");
		assertThat(result.accessToken()).isEqualTo("jwt-token");
		assertThat(result.expiresAt()).isEqualTo(TOKEN_EXPIRY);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userStore).save(captor.capture());
		assertThat(captor.getValue().passwordHash()).isEqualTo("hashed");
		assertThat(captor.getValue().createdAt()).isEqualTo(NOW);
	}

	@Test
	void rejectsDuplicateUsername() {
		when(userStore.existsByUsername("ash")).thenReturn(true);

		assertThatThrownBy(() -> service.register("ash", "pikachu12", "Ash"))
				.isInstanceOf(DuplicateUsernameException.class);
		verify(userStore, never()).save(any());
	}

	@Test
	void rejectsWeakPasswordOnRegister() {
		assertThatThrownBy(() -> service.register("ash", "short", "Ash"))
				.isInstanceOf(InvalidUserRegistrationException.class);
		verify(userStore, never()).existsByUsername(any());
	}

	@Test
	void logsInWithValidCredentials() {
		User user = new User(UUID.randomUUID(), "demo", "hashed", "Demo Trainer", NOW);
		when(userStore.findByUsername("demo")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("demo1234", "hashed")).thenReturn(true);
		when(tokenIssuer.issue(user)).thenReturn(new IssuedToken("jwt-token", TOKEN_EXPIRY));

		AuthResult result = service.login("Demo", "demo1234");

		assertThat(result.username()).isEqualTo("demo");
		assertThat(result.displayName()).isEqualTo("Demo Trainer");
		assertThat(result.accessToken()).isEqualTo("jwt-token");
	}

	@Test
	void rejectsUnknownUsernameWithoutLeakingExistence() {
		when(userStore.findByUsername("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.login("missing", "demo1234"))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("Invalid username or password");
		verify(passwordHasher, never()).matches(any(), any());
	}

	@Test
	void rejectsWrongPassword() {
		User user = new User(UUID.randomUUID(), "demo", "hashed", "Demo Trainer", NOW);
		when(userStore.findByUsername("demo")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("wrongpass", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> service.login("demo", "wrongpass"))
				.isInstanceOf(InvalidCredentialsException.class);
		verify(tokenIssuer, never()).issue(any());
	}

}
