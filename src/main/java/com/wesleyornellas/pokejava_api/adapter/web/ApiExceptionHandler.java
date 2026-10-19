package com.wesleyornellas.pokejava_api.adapter.web;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.domain.DuplicateUsernameException;
import com.wesleyornellas.pokejava_api.domain.InvalidCredentialsException;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.InvalidPokemonUpdateException;
import com.wesleyornellas.pokejava_api.domain.InvalidUserRegistrationException;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(PokemonNotFoundException.class)
	ResponseEntity<Map<String, Object>> notFound(PokemonNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateUsernameException.class)
	ResponseEntity<Map<String, Object>> conflict(DuplicateUsernameException ex) {
		return problem(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<Map<String, Object>> unauthorized(InvalidCredentialsException ex) {
		return problem(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler({
			InvalidPageRequestException.class,
			InvalidPokemonUpdateException.class,
			InvalidUserRegistrationException.class })
	ResponseEntity<Map<String, Object>> badRequest(RuntimeException ex) {
		return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, Object>> validationFailed(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		if (message.isBlank()) {
			message = "Request validation failed";
		}
		return problem(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Map<String, Object>> malformedBody(HttpMessageNotReadableException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Malformed request body");
	}

	@ExceptionHandler(CatalogUnavailableException.class)
	ResponseEntity<Map<String, Object>> catalogUnavailable(CatalogUnavailableException ex) {
		return problem(HttpStatus.BAD_GATEWAY, "Pokémon catalog is temporarily unavailable");
	}

	private static ResponseEntity<Map<String, Object>> problem(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}

}
