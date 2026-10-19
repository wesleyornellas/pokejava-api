package com.wesleyornellas.pokejava_api.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wesleyornellas.pokejava_api.application.AuthService;
import com.wesleyornellas.pokejava_api.application.AuthService.AuthResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

	private final AuthService authService;

	AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return toResponse(authService.register(request.username(), request.password(), request.displayName()));
	}

	@PostMapping("/login")
	AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return toResponse(authService.login(request.username(), request.password()));
	}

	private static AuthResponse toResponse(AuthResult result) {
		return new AuthResponse(
				result.id(),
				result.username(),
				result.displayName(),
				result.accessToken(),
				"Bearer",
				result.expiresAt());
	}

}
