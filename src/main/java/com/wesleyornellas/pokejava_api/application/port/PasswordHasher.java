package com.wesleyornellas.pokejava_api.application.port;

/**
 * One-way password hashing — implementation lives in the security adapter.
 */
public interface PasswordHasher {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);

}
