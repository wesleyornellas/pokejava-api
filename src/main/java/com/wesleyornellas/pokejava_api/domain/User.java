package com.wesleyornellas.pokejava_api.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Application user stored in {@code app_user}. Password is always a hash — never plain text.
 */
public record User(UUID id, String username, String passwordHash, String displayName, Instant createdAt) {

	public static final int MIN_USERNAME_LENGTH = 3;
	public static final int MAX_USERNAME_LENGTH = 50;
	public static final int MIN_PASSWORD_LENGTH = 8;
	public static final int MAX_PASSWORD_LENGTH = 72;
	public static final int MIN_DISPLAY_NAME_LENGTH = 1;
	public static final int MAX_DISPLAY_NAME_LENGTH = 100;

	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._-]+$");

	public User {
		if (id == null) {
			throw new InvalidUserRegistrationException("id is required");
		}
		if (username == null || username.isBlank()) {
			throw new InvalidUserRegistrationException("username is required");
		}
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new InvalidUserRegistrationException("passwordHash is required");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new InvalidUserRegistrationException("displayName is required");
		}
		if (createdAt == null) {
			throw new InvalidUserRegistrationException("createdAt is required");
		}
	}

	public static String normalizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new InvalidUserRegistrationException("username is required");
		}
		String normalized = username.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() < MIN_USERNAME_LENGTH || normalized.length() > MAX_USERNAME_LENGTH) {
			throw new InvalidUserRegistrationException(
					"username must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters");
		}
		if (!USERNAME_PATTERN.matcher(normalized).matches()) {
			throw new InvalidUserRegistrationException(
					"username may only contain lowercase letters, digits, '.', '_' and '-'");
		}
		return normalized;
	}

	public static String normalizeDisplayName(String displayName) {
		if (displayName == null || displayName.isBlank()) {
			throw new InvalidUserRegistrationException("displayName is required");
		}
		String trimmed = displayName.trim();
		if (trimmed.length() < MIN_DISPLAY_NAME_LENGTH || trimmed.length() > MAX_DISPLAY_NAME_LENGTH) {
			throw new InvalidUserRegistrationException(
					"displayName must be between " + MIN_DISPLAY_NAME_LENGTH + " and " + MAX_DISPLAY_NAME_LENGTH
							+ " characters");
		}
		return trimmed;
	}

	public static String requirePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new InvalidUserRegistrationException("password is required");
		}
		if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
			throw new InvalidUserRegistrationException(
					"password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters");
		}
		return password;
	}

	public static User create(String username, String passwordHash, String displayName, Instant createdAt) {
		return new User(UUID.randomUUID(), normalizeUsername(username), passwordHash, normalizeDisplayName(displayName),
				createdAt);
	}

}
