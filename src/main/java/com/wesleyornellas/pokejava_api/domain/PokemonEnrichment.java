package com.wesleyornellas.pokejava_api.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Proprietary fields stored alongside a locally replicated Pokémon.
 */
public record PokemonEnrichment(String localName, String region, List<String> tags) {

	public static final int MAX_TEXT_LENGTH = 150;
	public static final int MAX_TAGS = 20;
	public static final int MAX_TAG_LENGTH = 50;

	public PokemonEnrichment {
		tags = tags == null ? List.of() : List.copyOf(tags);
	}

	public static PokemonEnrichment of(String localName, String region, List<String> tags) {
		return new PokemonEnrichment(
				normalizeOptionalText(localName, "localName"),
				normalizeOptionalText(region, "region"),
				normalizeTags(tags));
	}

	private static String normalizeOptionalText(String value, String field) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		if (trimmed.length() > MAX_TEXT_LENGTH) {
			throw new InvalidPokemonUpdateException(field + " must be at most " + MAX_TEXT_LENGTH + " characters");
		}
		return trimmed;
	}

	private static List<String> normalizeTags(List<String> tags) {
		if (tags == null) {
			throw new InvalidPokemonUpdateException("tags must be provided");
		}
		if (tags.size() > MAX_TAGS) {
			throw new InvalidPokemonUpdateException("tags must contain at most " + MAX_TAGS + " values");
		}

		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		for (String tag : tags) {
			if (tag == null || tag.isBlank()) {
				throw new InvalidPokemonUpdateException("tags must not contain blank values");
			}
			String cleaned = tag.trim().toLowerCase(Locale.ROOT);
			if (cleaned.length() > MAX_TAG_LENGTH) {
				throw new InvalidPokemonUpdateException("each tag must be at most " + MAX_TAG_LENGTH + " characters");
			}
			normalized.add(cleaned);
		}
		return new ArrayList<>(normalized);
	}

}
