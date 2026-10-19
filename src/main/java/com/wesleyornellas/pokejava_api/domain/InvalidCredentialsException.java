package com.wesleyornellas.pokejava_api.domain;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Invalid username or password");
	}

}
