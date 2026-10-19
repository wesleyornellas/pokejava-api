package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pokemon")
class PokemonEntity {

	@Id
	private Integer id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(name = "image_url")
	private String imageUrl;

	private String category;

	private String description;

	@Column(name = "weight_kg", nullable = false)
	private double weightKg;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(columnDefinition = "text[]", nullable = false)
	private String[] abilities = new String[0];

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private List<StatEmbed> stats = new ArrayList<>();

	@Column(name = "local_name", length = 150)
	private String localName;

	@Column(length = 150)
	private String region;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(columnDefinition = "text[]", nullable = false)
	private String[] tags = new String[0];

	@Column(name = "synced_at", nullable = false)
	private Instant syncedAt;

	protected PokemonEntity() {
	}

	Integer getId() {
		return id;
	}

	void setId(Integer id) {
		this.id = id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	String getImageUrl() {
		return imageUrl;
	}

	void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	String getCategory() {
		return category;
	}

	void setCategory(String category) {
		this.category = category;
	}

	String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	double getWeightKg() {
		return weightKg;
	}

	void setWeightKg(double weightKg) {
		this.weightKg = weightKg;
	}

	String[] getAbilities() {
		return abilities;
	}

	void setAbilities(String[] abilities) {
		this.abilities = abilities;
	}

	List<StatEmbed> getStats() {
		return stats;
	}

	void setStats(List<StatEmbed> stats) {
		this.stats = stats;
	}

	String getLocalName() {
		return localName;
	}

	void setLocalName(String localName) {
		this.localName = localName;
	}

	String getRegion() {
		return region;
	}

	void setRegion(String region) {
		this.region = region;
	}

	String[] getTags() {
		return tags;
	}

	void setTags(String[] tags) {
		this.tags = tags;
	}

	Instant getSyncedAt() {
		return syncedAt;
	}

	void setSyncedAt(Instant syncedAt) {
		this.syncedAt = syncedAt;
	}

	record StatEmbed(String name, int baseStat) {
	}

}
