package com.wesleyornellas.pokejava_api.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

	public JwtProperties {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException("app.security.jwt.secret must be at least 32 characters");
		}
		if (expirationMinutes < 1) {
			throw new IllegalArgumentException("app.security.jwt.expiration-minutes must be at least 1");
		}
	}

}
