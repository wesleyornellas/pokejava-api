package com.wesleyornellas.pokejava_api.adapter.security;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.application.port.TokenIssuer;
import com.wesleyornellas.pokejava_api.domain.User;

@Component
class JwtTokenIssuer implements TokenIssuer {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;
	private final Clock clock;

	JwtTokenIssuer(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	public IssuedToken issue(User user) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(properties.expirationMinutes(), ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(user.id().toString())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("username", user.username())
				.claim("displayName", user.displayName())
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims)).getTokenValue();

		return new IssuedToken(token, expiresAt);
	}

}
