package com.wesleyornellas.pokejava_api.adapter.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wesleyornellas.pokejava_api.application.LocalPokemonService;
import com.wesleyornellas.pokejava_api.domain.InvalidPokemonUpdateException;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;

@WebMvcTest(controllers = LocalPokemonController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class LocalPokemonControllerTest {

	private static final LocalPokemon BULBASAUR = new LocalPokemon(
			1,
			"bulbasaur",
			"https://img/1.png",
			"Seed Pokémon",
			"A strange seed was planted on its back at birth.",
			6.9,
			List.of("overgrow"),
			List.of(new PokemonStat("hp", 45)),
			"Bulbasaurzinho",
			"Kanto",
			List.of("starter"),
			Instant.parse("2026-10-19T12:00:00Z"));

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LocalPokemonService localPokemonService;

	@Test
	void listsLocalPokemon() throws Exception {
		when(localPokemonService.list(0, 20)).thenReturn(new PageResult<>(List.of(BULBASAUR), 0, 20, 1));

		mockMvc.perform(get("/api/v1/local/pokemon"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
				.andExpect(jsonPath("$.content[0].localName").value("Bulbasaurzinho"))
				.andExpect(jsonPath("$.content[0].region").value("Kanto"))
				.andExpect(jsonPath("$.content[0].tags[0]").value("starter"));
	}

	@Test
	void returnsLocalDetail() throws Exception {
		when(localPokemonService.get("bulbasaur")).thenReturn(BULBASAUR);

		mockMvc.perform(get("/api/v1/local/pokemon/bulbasaur"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.tags[0]").value("starter"));
	}

	@Test
	void syncsPokemonFromCatalog() throws Exception {
		when(localPokemonService.sync("bulbasaur")).thenReturn(BULBASAUR);

		mockMvc.perform(post("/api/v1/local/pokemon/bulbasaur/sync"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("bulbasaur"))
				.andExpect(jsonPath("$.syncedAt").value("2026-10-19T12:00:00Z"));
	}

	@Test
	void mapsMissingLocalPokemonTo404() throws Exception {
		when(localPokemonService.get("missingno")).thenThrow(new PokemonNotFoundException("missingno"));

		mockMvc.perform(get("/api/v1/local/pokemon/missingno"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updatesLocalPokemon() throws Exception {
		when(localPokemonService.update(eq("bulbasaur"), eq("Bulbasaurzinho"), eq("Kanto"), eq(List.of("starter"))))
				.thenReturn(BULBASAUR);

		mockMvc.perform(put("/api/v1/local/pokemon/bulbasaur")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "localName": "Bulbasaurzinho",
						  "region": "Kanto",
						  "tags": ["starter"]
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localName").value("Bulbasaurzinho"))
				.andExpect(jsonPath("$.region").value("Kanto"));
	}

	@Test
	void mapsMissingPokemonOnUpdateTo404() throws Exception {
		when(localPokemonService.update(eq("missingno"), isNull(), isNull(), eq(List.of())))
				.thenThrow(new PokemonNotFoundException("missingno"));

		mockMvc.perform(put("/api/v1/local/pokemon/missingno")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "localName": null, "region": null, "tags": [] }
						"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void mapsDomainValidationFailureTo400() throws Exception {
		when(localPokemonService.update(eq("bulbasaur"), eq("ok"), eq("ok"), eq(List.of("  "))))
				.thenThrow(new InvalidPokemonUpdateException("tags must not contain blank values"));

		mockMvc.perform(put("/api/v1/local/pokemon/bulbasaur")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "localName": "ok", "region": "ok", "tags": ["  "] }
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("tags must not contain blank values"));
	}

	@Test
	void mapsMissingTagsFieldTo400() throws Exception {
		mockMvc.perform(put("/api/v1/local/pokemon/bulbasaur")
				.contentType(APPLICATION_JSON)
				.content("""
						{ "localName": "ok", "region": "ok" }
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void mapsMalformedJsonTo400() throws Exception {
		mockMvc.perform(put("/api/v1/local/pokemon/bulbasaur")
				.contentType(APPLICATION_JSON)
				.content("{ not-json }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Malformed request body"));
	}

}
