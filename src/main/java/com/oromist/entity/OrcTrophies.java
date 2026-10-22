package com.oromist.entity;

import com.oromist.registry.ModItems;
import net.minecraft.server.level.ServerLevel;

/**
 * Botín especial que deja un orco al morir a manos de un jugador (además de su tabla de botín):
 * <ul>
 *   <li><b>Notas de guerra</b>: siempre, si era un capitán o caudillo que murió de verdad.</li>
 *   <li><b>Hueso del augur</b>: los chamanes leen los huesos; uno con nombre siempre deja el suyo,
 *       un chamán raso a veces.</li>
 * </ul>
 */
public final class OrcTrophies {
	private static final float GRUNT_SHAMAN_BONE_CHANCE = 0.25f;

	private OrcTrophies() {
	}

	/** @param diedForGood {@code false} si engañó a la muerte (el chamán se llevó el cuerpo) */
	public static void drop(OrcEntity orc, ServerLevel level, boolean diedForGood) {
		if (!diedForGood) {
			return;
		}
		boolean named = orc.currentRank().isNamed();
		if (named) {
			orc.spawnAtLocation(level, ModItems.WAR_NOTES);
		}
		if (orc.archetype().isSpellcaster() && (named || orc.getRandom().nextFloat() < GRUNT_SHAMAN_BONE_CHANCE)) {
			orc.spawnAtLocation(level, ModItems.AUGUR_BONE);
		}
	}
}
