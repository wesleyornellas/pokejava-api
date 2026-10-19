package com.wesleyornellas.pokejava_api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class PokemonEnrichmentTest {

	@Test
	void normalizesAndDeduplicatesTags() {
		PokemonEnrichment enrichment = PokemonEnrichment.of("  Bulba  ", " Kanto ", List.of("Starter", "starter", " GRASS "));

		assertThat(enrichment.localName()).isEqualTo("Bulba");
		assertThat(enrichment.region()).isEqualTo("Kanto");
		assertThat(enrichment.tags()).containsExactly("starter", "grass");
	}

	@Test
	void treatsBlankOptionalTextAsNull() {
		PokemonEnrichment enrichment = PokemonEnrichment.of("   ", "", List.of());

		assertThat(enrichment.localName()).isNull();
		assertThat(enrichment.region()).isNull();
		assertThat(enrichment.tags()).isEmpty();
	}

	@Test
	void rejectsNullTags() {
		assertThatThrownBy(() -> PokemonEnrichment.of("name", "region", null))
				.isInstanceOf(InvalidPokemonUpdateException.class)
				.hasMessageContaining("tags");
	}

	@Test
	void rejectsBlankTag() {
		assertThatThrownBy(() -> PokemonEnrichment.of(null, null, List.of("ok", "  ")))
				.isInstanceOf(InvalidPokemonUpdateException.class)
				.hasMessageContaining("blank");
	}

	@Test
	void rejectsTooManyTags() {
		List<String> tags = IntStream.rangeClosed(1, 21).mapToObj(i -> "tag-" + i).toList();

		assertThatThrownBy(() -> PokemonEnrichment.of(null, null, tags))
				.isInstanceOf(InvalidPokemonUpdateException.class)
				.hasMessageContaining("at most 20");
	}

	@Test
	void rejectsOversizedLocalName() {
		String tooLong = String.join("", Collections.nCopies(151, "a"));

		assertThatThrownBy(() -> PokemonEnrichment.of(tooLong, null, List.of()))
				.isInstanceOf(InvalidPokemonUpdateException.class)
				.hasMessageContaining("localName");
	}

}
