package com.oromist.nemesis.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Repositorio en memoria para probar el dominio sin Minecraft. */
public final class InMemoryRepository implements NemesisRepository {
	private final Map<UUID, OrcProfile> profiles = new LinkedHashMap<>();

	@Override
	public Optional<OrcProfile> find(UUID orcId) {
		return Optional.ofNullable(profiles.get(orcId));
	}

	@Override
	public Collection<OrcProfile> all() {
		return profiles.values();
	}

	@Override
	public void save(OrcProfile profile) {
		profiles.put(profile.id(), profile);
	}

	@Override
	public void markChanged() {
	}
}
