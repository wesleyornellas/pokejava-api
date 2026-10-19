package com.wesleyornellas.pokejava_api.adapter.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wesleyornellas.pokejava_api.application.port.CatalogUnavailableException;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(PokemonNotFoundException.class)
	ResponseEntity<Map<String, Object>> notFound(PokemonNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(InvalidPageRequestException.class)
	ResponseEntity<Map<String, Object>> badRequest(InvalidPageRequestException ex) {
		return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(CatalogUnavailableException.class)
	ResponseEntity<Map<String, Object>> catalogUnavailable(CatalogUnavailableException ex) {
		return problem(HttpStatus.BAD_GATEWAY, "Pokémon catalog is temporarily unavailable");
	}

	private static ResponseEntity<Map<String, Object>> problem(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(
				"timestamp", Instant.now().toString(),
				"status", status.value(),
				"error", status.getReasonPhrase(),
				"message", message));
	}

}
