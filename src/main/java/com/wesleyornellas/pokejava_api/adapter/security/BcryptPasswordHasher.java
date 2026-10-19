package com.wesleyornellas.pokejava_api.adapter.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.application.port.PasswordHasher;

@Component
class BcryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;

	BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return passwordEncoder.matches(rawPassword, passwordHash);
	}

}
