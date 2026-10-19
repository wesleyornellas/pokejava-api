package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByUsername(String username);

	boolean existsByUsername(String username);

}
