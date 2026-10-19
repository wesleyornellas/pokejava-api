package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPokemonRepository extends JpaRepository<PokemonEntity, Integer> {

	Optional<PokemonEntity> findByName(String name);

}
