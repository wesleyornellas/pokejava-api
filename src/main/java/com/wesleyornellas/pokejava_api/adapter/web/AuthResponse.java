package com.wesleyornellas.pokejava_api.adapter.web;

import java.time.Instant;
import java.util.UUID;

record AuthResponse(
		UUID id,
		String username,
		String displayName,
		String accessToken,
		String tokenType,
		Instant expiresAt) {
}
