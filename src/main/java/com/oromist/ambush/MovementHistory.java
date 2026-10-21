package com.oromist.ambush;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

/** Recuerda la posición de cada jugador en la evaluación anterior para saber si está viajando. */
public final class MovementHistory {
	private final Map<UUID, Vec3> lastPositions = new HashMap<>();

	public Optional<Vec3> previousPosition(UUID playerId) {
		return Optional.ofNullable(lastPositions.get(playerId));
	}

	public void record(UUID playerId, Vec3 position) {
		lastPositions.put(playerId, position);
	}

	public void forget(UUID playerId) {
		lastPositions.remove(playerId);
	}
}
