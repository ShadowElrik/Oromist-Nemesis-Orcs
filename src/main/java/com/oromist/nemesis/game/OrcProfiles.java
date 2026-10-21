package com.oromist.nemesis.game;

import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import java.util.Optional;

/** Operaciones que conectan una entidad orco con su perfil Némesis. */
public final class OrcProfiles {
	private OrcProfiles() {
	}

	public static Optional<OrcProfile> of(OrcEntity orc, NemesisService service) {
		return orc.profileId().flatMap(service::find);
	}

	/**
	 * Devuelve el perfil del orco, creándolo si aún no lo tenía. Así un soldado anónimo
	 * se convierte en un orco "recordado" en cuanto hace algo memorable.
	 */
	public static OrcProfile ensure(OrcEntity orc, NemesisService service) {
		Optional<OrcProfile> existing = of(orc, service);
		if (existing.isPresent()) {
			return existing.get();
		}
		OrcRank rank = orc.pendingRank().orElse(OrcRank.GRUNT);
		OrcProfile profile = service.createProfile(orc.archetype(), orc.sizeScale(), orc.skinVariant(), rank);
		boolean freshBody = rank != OrcRank.GRUNT;
		orc.bindProfile(profile, service.beginIncarnation(profile), freshBody);
		return profile;
	}
}
