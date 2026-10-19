package com.wesleyornellas.pokejava_api.adapter.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wesleyornellas.pokejava_api.application.AuthService;
import com.wesleyornellas.pokejava_api.application.AuthService.AuthResult;
import com.wesleyornellas.pokejava_api.domain.DuplicateUsernameException;
import com.wesleyornellas.pokejava_api.domain.InvalidCredentialsException;
import com.wesleyornellas.pokejava_api.domain.InvalidUserRegistrationException;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class AuthControllerTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final Instant EXPIRES = Instant.parse("2026-10-19T16:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void registersUser() throws Exception {
		when(authService.register(eq("ash"), eq("pikachu12"), eq("Ash Ketchum")))
				.thenReturn(new AuthResult(USER_ID, "ash", "Ash Ketchum", "jwt-token", EXPIRES));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "username": "ash",
						  "password": "pikachu12",
						  "displayName": "Ash Ketchum"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(USER_ID.toString()))
				.andExpect(jsonPath("$.username").value("ash"))
				.andExpect(jsonPath("$.displayName").value("Ash Ketchum"))
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresAt").value("2026-10-19T16:00:00Z"));
	}

	@Test
	void mapsDuplicateUsernameTo409() throws Exception {
		when(authService.register(eq("ash"), eq("pikachu12"), eq("Ash")))
				.thenThrow(new DuplicateUsernameException("ash"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "username": "ash", "password": "pikachu12", "displayName": "Ash" }
						"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Username already taken: ash"));
	}

	@Test
	void mapsDomainRegistrationFailureTo400() throws Exception {
		when(authService.register(eq("ash"), eq("pikachu12"), eq("Ash")))
				.thenThrow(new InvalidUserRegistrationException("username may only contain lowercase letters, digits, '.', '_' and '-'"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "username": "ash", "password": "pikachu12", "displayName": "Ash" }
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void mapsBeanValidationFailureTo400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "username": "ab", "password": "short", "displayName": "" }
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void logsInUser() throws Exception {
		when(authService.login(eq("demo"), eq("demo1234")))
				.thenReturn(new AuthResult(USER_ID, "demo", "Demo Trainer", "jwt-token", EXPIRES));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "username": "demo", "password": "demo1234" }
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.username").value("demo"));
	}

	@Test
	void mapsInvalidCredentialsTo401() throws Exception {
		when(authService.login(eq("demo"), eq("wrongpass")))
				.thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "username": "demo", "password": "wrongpass" }
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid username or password"));
	}

}
