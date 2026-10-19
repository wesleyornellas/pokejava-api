package com.wesleyornellas.pokejava_api.domain;

public class DuplicateUsernameException extends RuntimeException {

	public DuplicateUsernameException(String username) {
		super("Username already taken: " + username);
	}

}
