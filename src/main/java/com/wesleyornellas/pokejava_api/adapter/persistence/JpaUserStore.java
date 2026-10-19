package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.application.port.UserStore;
import com.wesleyornellas.pokejava_api.domain.User;

@Component
class JpaUserStore implements UserStore {

	private final SpringDataUserRepository repository;

	JpaUserStore(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public User save(User user) {
		return toDomain(repository.save(toEntity(user)));
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repository.findByUsername(username).map(JpaUserStore::toDomain);
	}

	@Override
	public boolean existsByUsername(String username) {
		return repository.existsByUsername(username);
	}

	private static UserEntity toEntity(User user) {
		UserEntity entity = new UserEntity();
		entity.setId(user.id());
		entity.setUsername(user.username());
		entity.setPasswordHash(user.passwordHash());
		entity.setDisplayName(user.displayName());
		entity.setCreatedAt(user.createdAt());
		return entity;
	}

	private static User toDomain(UserEntity entity) {
		return new User(
				entity.getId(),
				entity.getUsername(),
				entity.getPasswordHash(),
				entity.getDisplayName(),
				entity.getCreatedAt());
	}

}
