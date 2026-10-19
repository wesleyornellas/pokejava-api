package com.wesleyornellas.pokejava_api.adapter.web;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for replacing proprietary enrichment fields on a locally stored Pokémon.
 */
record UpdateLocalPokemonRequest(
		@Size(max = 150) String localName,
		@Size(max = 150) String region,
		@NotNull @Size(max = 20) List<@Size(max = 50) String> tags) {
}
