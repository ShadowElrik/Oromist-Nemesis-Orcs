package com.oromist.nemesis.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Un recuerdo concreto que un orco guarda sobre un jugador.
 *
 * @param type       qué ocurrió
 * @param playerId   jugador involucrado
 * @param playerName nombre del jugador en ese momento (para diálogos aunque esté desconectado)
 * @param gameTime   tick del mundo en que ocurrió (para calcular "hace cuántos días")
 * @param place      descripción breve del lugar (ej. id del bioma), puede estar vacía
 */
public record Memory(MemoryType type, UUID playerId, String playerName, long gameTime, String place) {
	/** Duración de un día de Minecraft en ticks. */
	public static final long TICKS_PER_DAY = 24000L;

	public Memory {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(playerId, "playerId");
		playerName = playerName == null ? "" : playerName;
		place = place == null ? "" : place;
	}

	/** Días de juego completos transcurridos desde el recuerdo (nunca negativo). */
	public long daysAgo(long now) {
		return Math.max(0, (now - gameTime) / TICKS_PER_DAY);
	}

	public boolean isAbout(UUID player) {
		return playerId.equals(player);
	}
}
