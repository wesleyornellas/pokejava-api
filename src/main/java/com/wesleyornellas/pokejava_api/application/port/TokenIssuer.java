package com.wesleyornellas.pokejava_api.application.port;

import java.time.Instant;

import com.wesleyornellas.pokejava_api.domain.User;

/**
 * Issues opaque/JWT access tokens for authenticated users.
 */
public interface TokenIssuer {

	IssuedToken issue(User user);

	record IssuedToken(String accessToken, Instant expiresAt) {
	}

}
