package com.wesleyornellas.pokejava_api.domain;

public class InvalidUserRegistrationException extends RuntimeException {

	public InvalidUserRegistrationException(String message) {
		super(message);
	}

}
