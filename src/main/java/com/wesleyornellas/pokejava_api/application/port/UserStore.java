package com.wesleyornellas.pokejava_api.application.port;

import java.util.Optional;

import com.wesleyornellas.pokejava_api.domain.User;

/**
 * Persistence for application users.
 */
public interface UserStore {

	User save(User user);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

}
