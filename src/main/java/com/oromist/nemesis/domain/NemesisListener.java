package com.oromist.nemesis.domain;

import java.util.UUID;

/**
 * Observador de los hechos importantes del sistema Némesis. Permite añadir
 * reacciones nuevas (mensajes, sonidos, logros...) sin modificar {@link NemesisService}.
 */
public interface NemesisListener {
	default void onPromoted(OrcProfile profile, OrcRank newRank) {
	}

	default void onCheatedDeath(OrcProfile profile, UUID playerId) {
	}

	default void onDied(OrcProfile profile, UUID playerId) {
	}

	default void onKilledPlayer(OrcProfile profile, UUID playerId) {
	}
}
