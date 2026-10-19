package com.wesleyornellas.pokejava_api.adapter.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wesleyornellas.pokejava_api.application.PokemonQueryService;
import com.wesleyornellas.pokejava_api.domain.EvolutionStage;
import com.wesleyornellas.pokejava_api.domain.InvalidPageRequestException;
import com.wesleyornellas.pokejava_api.domain.PageResult;
import com.wesleyornellas.pokejava_api.domain.PokemonDetail;
import com.wesleyornellas.pokejava_api.domain.PokemonNotFoundException;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;
import com.wesleyornellas.pokejava_api.domain.PokemonSummary;

@WebMvcTest(controllers = PokemonController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PokemonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PokemonQueryService pokemonQueryService;

	@Test
	void listsPokemon() throws Exception {
		when(pokemonQueryService.listPokemon(0, 20)).thenReturn(new PageResult<>(
				List.of(new PokemonSummary(1, "bulbasaur", "https://img/1.png", "Seed Pokémon", 6.9,
						List.of("overgrow"))),
				0,
				20,
				1025));

		mockMvc.perform(get("/api/v1/pokemon"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
				.andExpect(jsonPath("$.totalElements").value(1025));
	}

	@Test
	void returnsDetail() throws Exception {
		when(pokemonQueryService.getPokemon("bulbasaur")).thenReturn(new PokemonDetail(
				1,
				"bulbasaur",
				"https://img/1.png",
				"Seed Pokémon",
				"A strange seed was planted on its back at birth.",
				6.9,
				List.of("overgrow", "chlorophyll"),
				List.of(new PokemonStat("hp", 45)),
				new EvolutionStage("bulbasaur", List.of(new EvolutionStage("ivysaur", List.of())))));

		mockMvc.perform(get("/api/v1/pokemon/bulbasaur"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("bulbasaur"))
				.andExpect(jsonPath("$.stats[0].name").value("hp"))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].name").value("ivysaur"));
	}

	@Test
	void mapsMissingPokemonTo404() throws Exception {
		when(pokemonQueryService.getPokemon("missingno")).thenThrow(new PokemonNotFoundException("missingno"));

		mockMvc.perform(get("/api/v1/pokemon/missingno"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Pokémon not found: missingno"));
	}

	@Test
	void mapsInvalidPageTo400() throws Exception {
		when(pokemonQueryService.listPokemon(-1, 20)).thenThrow(new InvalidPageRequestException("page must be zero or greater"));

		mockMvc.perform(get("/api/v1/pokemon").param("page", "-1"))
				.andExpect(status().isBadRequest());
	}

}
