package com.oromist.entity;

import com.oromist.nemesis.domain.OrcTrait;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Puntos de extensión que la entidad orco invoca y que implementa la capa Némesis.
 *
 * <p>Minecraft crea las entidades por reflexión/fábricas, así que no podemos inyectar
 * dependencias por constructor. Esta interfaz mantiene a {@link OrcEntity} desacoplada
 * del sistema Némesis: la entidad solo "avisa" y quien la implemente decide qué hacer.</p>
 */
public interface OrcHooks {
	/** Se llama una vez por segundo en el servidor. */
	default void onServerSecond(OrcEntity orc) {
	}

	/** El orco empieza a huir de {@code threat}. */
	default void onRetreatStarted(OrcEntity orc, LivingEntity threat) {
	}

	/** El orco escapó de {@code threat}; justo después la entidad desaparece. */
	default void onEscaped(OrcEntity orc, LivingEntity threat) {
	}

	/** El jugador al que perseguía el orco se alejó demasiado (huyó). */
	default void onTargetFled(OrcEntity orc, ServerPlayer player) {
	}

	/** El jugador acaba de ver en acción uno de los rasgos del orco (p. ej. sus flechas no le hacen nada). */
	default void onTraitShown(OrcEntity orc, ServerPlayer player, OrcTrait trait) {
	}

	OrcHooks NONE = new OrcHooks() {
	};
}
