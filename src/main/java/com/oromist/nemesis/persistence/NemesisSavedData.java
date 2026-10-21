package com.oromist.nemesis.persistence;

import com.mojang.serialization.Codec;
import com.oromist.OromistNPC;
import com.oromist.nemesis.domain.NemesisRepository;
import com.oromist.nemesis.domain.OrcProfile;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Implementación de {@link NemesisRepository} sobre el sistema de guardado del
 * mundo de Minecraft. Se guarda una sola vez por servidor (no por dimensión),
 * así un némesis puede perseguirte entre dimensiones.
 */
public final class NemesisSavedData extends SavedData implements NemesisRepository {
	/** Perfiles muertos que se conservan como historial antes de purgarse. */
	private static final int MAX_DEAD_PROFILES = 64;

	private static final Codec<NemesisSavedData> CODEC = NemesisCodecs.PROFILE.listOf()
			.fieldOf("profiles").codec()
			.xmap(NemesisSavedData::new, data -> List.copyOf(data.profiles.values()));

	public static final SavedDataType<NemesisSavedData> TYPE = new SavedDataType<>(
			OromistNPC.id("nemesis"), NemesisSavedData::new, CODEC, null);

	private final Map<UUID, OrcProfile> profiles = new LinkedHashMap<>();

	public NemesisSavedData() {
	}

	private NemesisSavedData(List<OrcProfile> loaded) {
		loaded.forEach(p -> profiles.put(p.id(), p));
	}

	public static NemesisSavedData get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	@Override
	public Optional<OrcProfile> find(UUID orcId) {
		return Optional.ofNullable(profiles.get(orcId));
	}

	@Override
	public Collection<OrcProfile> all() {
		return Collections.unmodifiableCollection(profiles.values());
	}

	@Override
	public void save(OrcProfile profile) {
		profiles.put(profile.id(), profile);
		markChanged();
	}

	@Override
	public void markChanged() {
		pruneDeadProfiles();
		setDirty();
	}

	private void pruneDeadProfiles() {
		long dead = profiles.values().stream().filter(p -> !p.isAlive()).count();
		if (dead <= MAX_DEAD_PROFILES) {
			return;
		}
		profiles.values().stream()
				.filter(p -> !p.isAlive())
				.limit(dead - MAX_DEAD_PROFILES)
				.map(OrcProfile::id)
				.toList()
				.forEach(profiles::remove);
	}
}
