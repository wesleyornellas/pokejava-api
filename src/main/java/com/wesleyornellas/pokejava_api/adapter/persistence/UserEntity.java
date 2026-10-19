package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
class UserEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserEntity() {
	}

	UUID getId() {
		return id;
	}

	void setId(UUID id) {
		this.id = id;
	}

	String getUsername() {
		return username;
	}

	void setUsername(String username) {
		this.username = username;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	String getDisplayName() {
		return displayName;
	}

	void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
