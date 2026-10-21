package com.oromist.nemesis.persistence;

import com.mojang.serialization.Codec;
import com.oromist.OromistNPC;
import com.oromist.nemesis.domain.NemesisRepository;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.ProfileRetentionPolicy;
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
 *
 * <p>La lectura es tolerante: un perfil corrupto se descarta sin perder los demás.
 * Los límites de cuántos perfiles se conservan los define {@link ProfileRetentionPolicy}.</p>
 */
public final class NemesisSavedData extends SavedData implements NemesisRepository {
	/** Cada cuántos cambios se revisan los límites (evita recorrer todo en cada recuerdo nuevo). */
	private static final int PRUNE_EVERY = 32;
	private static final ProfileRetentionPolicy RETENTION = ProfileRetentionPolicy.DEFAULT;

	private static final Codec<NemesisSavedData> CODEC = NemesisCodecs.lenientList(NemesisCodecs.PROFILE)
			.fieldOf("profiles").codec()
			.xmap(NemesisSavedData::new, data -> List.copyOf(data.profiles.values()));

	public static final SavedDataType<NemesisSavedData> TYPE = new SavedDataType<>(
			OromistNPC.id("nemesis"), NemesisSavedData::new, CODEC, null);

	private final Map<UUID, OrcProfile> profiles = new LinkedHashMap<>();
	private int changesSincePrune;

	public NemesisSavedData() {
	}

	private NemesisSavedData(List<OrcProfile> loaded) {
		loaded.forEach(p -> profiles.put(p.id(), p));
		prune();
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
		if (++changesSincePrune >= PRUNE_EVERY) {
			prune();
		}
		setDirty();
	}

	private void prune() {
		changesSincePrune = 0;
		RETENTION.profilesToRemove(profiles.values()).forEach(profiles::remove);
	}
}
