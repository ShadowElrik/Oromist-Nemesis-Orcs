package com.oromist.nemesis.domain;

import java.util.UUID;

/**
 * Datos de "quién, cuándo y dónde" de un evento. La capa de Minecraft lo construye
 * y el dominio lo convierte en {@link Memory} según el tipo de hecho.
 */
public record EncounterContext(UUID playerId, String playerName, long gameTime, String place) {
	public Memory toMemory(MemoryType type) {
		return new Memory(type, playerId, playerName, gameTime, place);
	}
}
