package com.wesleyornellas.pokejava_api.application;

import java.time.Clock;

import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;
import com.wesleyornellas.pokejava_api.application.port.TokenIssuer;
import com.wesleyornellas.pokejava_api.application.port.TokenIssuer.IssuedToken;
import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.DuplicateUsernameException;
import com.wesleyornellas.pokejava_api.domain.InvalidCredentialsException;
import com.wesleyornellas.pokejava_api.domain.User;

public class AuthService {

	private final UserStore userStore;
	private final PasswordHasher passwordHasher;
	private final TokenIssuer tokenIssuer;
	private final Clock clock;

	public AuthService(UserStore userStore, PasswordHasher passwordHasher, TokenIssuer tokenIssuer, Clock clock) {
		this.userStore = userStore;
		this.passwordHasher = passwordHasher;
		this.tokenIssuer = tokenIssuer;
		this.clock = clock;
	}

	public AuthResult register(String username, String password, String displayName) {
		String normalizedUsername = User.normalizeUsername(username);
		String normalizedDisplayName = User.normalizeDisplayName(displayName);
		String rawPassword = User.requirePassword(password);

		if (userStore.existsByUsername(normalizedUsername)) {
			throw new DuplicateUsernameException(normalizedUsername);
		}

		User user = User.create(
				normalizedUsername,
				passwordHasher.hash(rawPassword),
				normalizedDisplayName,
				clock.instant());
		User saved = userStore.save(user);
		return toResult(saved);
	}

	public AuthResult login(String username, String password) {
		String normalizedUsername = User.normalizeUsername(username);
		String rawPassword = User.requirePassword(password);

		User user = userStore.findByUsername(normalizedUsername)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		return toResult(user);
	}

	private AuthResult toResult(User user) {
		IssuedToken token = tokenIssuer.issue(user);
		return new AuthResult(
				user.id(),
				user.username(),
				user.displayName(),
				token.accessToken(),
				token.expiresAt());
	}

	public record AuthResult(
			java.util.UUID id,
			String username,
			String displayName,
			String accessToken,
			java.time.Instant expiresAt) {
	}

}
