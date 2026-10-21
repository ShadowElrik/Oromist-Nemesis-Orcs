package com.oromist.nemesis.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registro de qué orcos con perfil están "despiertos" (su entidad está cargada y
 * actualizándose). Cada orco avisa una vez por segundo; si deja de avisar, se considera
 * inactivo.
 *
 * <p>En multijugador evita que una emboscada "teletransporte" a un némesis que ahora
 * mismo está peleando con otro jugador.</p>
 */
public final class ActiveOrcs {
	/** Margen sin latidos tras el cual un orco deja de contar como activo. */
	private static final long TIMEOUT_TICKS = 60;

	private final Map<UUID, Long> lastHeartbeat = new HashMap<>();

	public void heartbeat(UUID profileId, long gameTime) {
		lastHeartbeat.put(profileId, gameTime);
	}

	public boolean isActive(UUID profileId, long gameTime) {
		Long last = lastHeartbeat.get(profileId);
		return last != null && gameTime - last <= TIMEOUT_TICKS;
	}

	/** Elimina entradas viejas para que el mapa no crezca indefinidamente. */
	public void purge(long gameTime) {
		lastHeartbeat.values().removeIf(last -> gameTime - last > TIMEOUT_TICKS);
	}

	public void clear() {
		lastHeartbeat.clear();
	}
}
