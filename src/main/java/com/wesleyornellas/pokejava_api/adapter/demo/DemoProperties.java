package com.wesleyornellas.pokejava_api.adapter.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(
		boolean seedEnabled,
		String username,
		String password,
		String displayName) {

	public DemoProperties {
		if (username == null || username.isBlank()) {
			username = "demo";
		}
		if (password == null || password.isBlank()) {
			password = "demo1234";
		}
		if (displayName == null || displayName.isBlank()) {
			displayName = "Demo Trainer";
		}
	}

}
