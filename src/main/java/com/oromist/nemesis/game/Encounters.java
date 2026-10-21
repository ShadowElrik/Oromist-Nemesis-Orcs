package com.oromist.nemesis.game;

import com.oromist.nemesis.domain.EncounterContext;
import net.minecraft.server.level.ServerPlayer;

/** Traduce el estado del juego a {@link EncounterContext} del dominio. */
public final class Encounters {
	private Encounters() {
	}

	public static EncounterContext of(ServerPlayer player) {
		String biome = player.level().getBiome(player.blockPosition())
				.unwrapKey()
				.map(key -> key.identifier().toString())
				.orElse("");
		return new EncounterContext(player.getUUID(), player.getPlainTextName(), player.level().getGameTime(), biome);
	}
}
