package com.oromist.nemesis.domain;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de perfiles. El dominio no sabe cómo se guardan
 * (SavedData de Minecraft, JSON, base de datos...): solo depende de esta interfaz
 * (inversión de dependencias).
 */
public interface NemesisRepository {
	Optional<OrcProfile> find(UUID orcId);

	Collection<OrcProfile> all();

	void save(OrcProfile profile);

	/** Notifica que un perfil existente cambió y debe persistirse. */
	void markChanged();
}
