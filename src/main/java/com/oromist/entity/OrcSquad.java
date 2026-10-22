package com.oromist.entity;

import com.oromist.OromistNPC;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Cómo luchan los orcos en grupo:
 * <ul>
 *   <li><b>Grito de guerra</b>: el primero que ve a un humano avisa a los orcos cercanos.</li>
 *   <li><b>Liderazgo</b>: un capitán o caudillo cerca inspira a los soldados rasos
 *       (golpean más fuerte y no huyen).</li>
 *   <li><b>Protección del chamán</b>: un chamán cerca puede traer de vuelta a un capitán caído.</li>
 * </ul>
 */
public final class OrcSquad {
	public static final double WAR_CRY_RADIUS = 20.0;
	public static final double LEADERSHIP_RADIUS = 12.0;
	public static final double SHAMAN_WARD_RADIUS = 24.0;
	private static final double INSPIRED_DAMAGE_BONUS = 0.25;
	private static final Identifier INSPIRED_MODIFIER = OromistNPC.id("inspired");

	private OrcSquad() {
	}

	/**
	 * Grito de guerra: los orcos cercanos que no estén ocupados se unen a la caza.
	 *
	 * @return cuántos orcos respondieron
	 */
	public static int warCry(OrcEntity caller, LivingEntity prey) {
		if (!(caller.level() instanceof ServerLevel level)) {
			return 0;
		}
		List<OrcEntity> listeners = level.getEntitiesOfClass(OrcEntity.class, caller.getBoundingBox().inflate(WAR_CRY_RADIUS),
				orc -> orc != caller && orc.isAlive() && orc.getTarget() == null);
		listeners.forEach(orc -> orc.answerWarCry(prey));
		level.playSound(null, caller.getX(), caller.getY(), caller.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE,
				0.7F, 1.4F / Math.max(0.5F, caller.sizeScale()));
		return listeners.size();
	}

	/** ¿Hay un orco con nombre (capitán o caudillo) vivo cerca de este soldado? */
	public static boolean hasLeaderNearby(OrcEntity grunt) {
		return !grunt.level().getEntitiesOfClass(OrcEntity.class, grunt.getBoundingBox().inflate(LEADERSHIP_RADIUS),
				orc -> orc != grunt && orc.isAlive() && orc.currentRank().isNamed()).isEmpty();
	}

	/** ¿Hay un chamán vivo cerca del orco que acaba de caer? */
	public static boolean hasShamanNearby(OrcEntity fallen) {
		return !fallen.level().getEntitiesOfClass(OrcEntity.class, fallen.getBoundingBox().inflate(SHAMAN_WARD_RADIUS),
				orc -> orc != fallen && orc.isAlive() && orc.archetype().isSpellcaster()).isEmpty();
	}

	/** Aplica o retira la inspiración del líder (más daño). Se llama una vez por segundo. */
	public static void updateInspiration(OrcEntity orc, boolean inspired) {
		AttributeInstance damage = orc.getAttribute(Attributes.ATTACK_DAMAGE);
		if (damage == null) {
			return;
		}
		if (inspired && !damage.hasModifier(INSPIRED_MODIFIER)) {
			damage.addTransientModifier(new AttributeModifier(INSPIRED_MODIFIER, INSPIRED_DAMAGE_BONUS,
					AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		} else if (!inspired && damage.hasModifier(INSPIRED_MODIFIER)) {
			damage.removeModifier(INSPIRED_MODIFIER);
		}
	}
}
