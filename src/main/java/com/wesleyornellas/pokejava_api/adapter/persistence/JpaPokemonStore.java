package com.wesleyornellas.pokejava_api.adapter.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.wesleyornellas.pokejava_api.adapter.persistence.PokemonEntity.StatEmbed;
import com.wesleyornellas.pokejava_api.application.port.PokemonStore;
import com.wesleyornellas.pokejava_api.domain.LocalPokemon;
import com.wesleyornellas.pokejava_api.domain.PokemonStat;

@Component
class JpaPokemonStore implements PokemonStore {

	private final SpringDataPokemonRepository repository;

	JpaPokemonStore(SpringDataPokemonRepository repository) {
		this.repository = repository;
	}

	@Override
	public LocalPokemon save(LocalPokemon pokemon) {
		return toDomain(repository.save(toEntity(pokemon)));
	}

	@Override
	public Optional<LocalPokemon> findById(int id) {
		return repository.findById(id).map(JpaPokemonStore::toDomain);
	}

	@Override
	public Optional<LocalPokemon> findByName(String name) {
		return repository.findByName(name).map(JpaPokemonStore::toDomain);
	}

	@Override
	public List<LocalPokemon> findPage(int offset, int limit) {
		int page = limit == 0 ? 0 : offset / limit;
		return repository.findAll(PageRequest.of(page, limit, Sort.by("id"))).stream()
				.map(JpaPokemonStore::toDomain)
				.toList();
	}

	@Override
	public long count() {
		return repository.count();
	}

	private static PokemonEntity toEntity(LocalPokemon pokemon) {
		PokemonEntity entity = new PokemonEntity();
		entity.setId(pokemon.id());
		entity.setName(pokemon.name());
		entity.setImageUrl(pokemon.imageUrl());
		entity.setCategory(pokemon.category());
		entity.setDescription(pokemon.description());
		entity.setWeightKg(pokemon.weightKg());
		entity.setAbilities(pokemon.abilities().toArray(String[]::new));
		entity.setStats(pokemon.stats().stream()
				.map(stat -> new StatEmbed(stat.name(), stat.baseStat()))
				.toList());
		entity.setLocalName(pokemon.localName());
		entity.setRegion(pokemon.region());
		entity.setTags(pokemon.tags().toArray(String[]::new));
		entity.setSyncedAt(pokemon.syncedAt());
		return entity;
	}

	private static LocalPokemon toDomain(PokemonEntity entity) {
		List<String> abilities = entity.getAbilities() == null ? List.of() : Arrays.asList(entity.getAbilities());
		List<String> tags = entity.getTags() == null ? List.of() : Arrays.asList(entity.getTags());
		List<PokemonStat> stats = entity.getStats() == null
				? List.of()
				: entity.getStats().stream().map(stat -> new PokemonStat(stat.name(), stat.baseStat())).toList();

		return new LocalPokemon(
				entity.getId(),
				entity.getName(),
				entity.getImageUrl(),
				entity.getCategory(),
				entity.getDescription(),
				entity.getWeightKg(),
				abilities,
				stats,
				entity.getLocalName(),
				entity.getRegion(),
				tags,
				entity.getSyncedAt());
	}

}
