package com.oromist.entity.magic;

import com.oromist.entity.OrcEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Los hechizos de los chamanes orcos. Cada método es un hechizo completo (efecto, partículas
 * y sonido); las metas de IA ({@code ShamanSpellGoal}, {@code ShamanWarBloodGoal}) solo deciden
 * <i>cuándo</i> lanzarlos.
 */
public final class ShamanSpells {
	/** Número de "garras de hueso" que brotan en línea hacia el objetivo. */
	private static final int SPIKE_COUNT = 8;
	private static final double SPIKE_SPACING = 1.25;
	/** Radio en el que "Sangre de guerra" alcanza a los aliados. */
	public static final double WAR_BLOOD_RADIUS = 10.0;
	private static final int WAR_BLOOD_TICKS = 8 * 20;

	private ShamanSpells() {
	}

	/** Fuego verde: un proyectil mágico dirigido al objetivo. */
	public static void greenFire(OrcEntity shaman, LivingEntity target) {
		if (!(shaman.level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 direction = new Vec3(
				target.getX() - shaman.getX(),
				target.getY(0.5) - shaman.getEyeY(),
				target.getZ() - shaman.getZ()).normalize();
		GreenFireball fireball = new GreenFireball(level, shaman, direction);
		fireball.setPos(shaman.getX() + direction.x, shaman.getEyeY() - 0.1, shaman.getZ() + direction.z);
		level.addFreshEntity(fireball);
		level.playSound(null, shaman.getX(), shaman.getY(), shaman.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
				1.0F, 0.6F + shaman.getRandom().nextFloat() * 0.2F);
	}

	/**
	 * Garras de hueso: una hilera de colmillos que brota del suelo hacia el objetivo
	 * (las mismas que invocan los evocadores). Sirve para alejar a quien se acerca demasiado.
	 */
	public static void boneSpikes(OrcEntity shaman, LivingEntity target) {
		if (!(shaman.level() instanceof ServerLevel level)) {
			return;
		}
		double minY = Math.min(target.getY(), shaman.getY());
		double maxY = Math.max(target.getY(), shaman.getY()) + 1.0;
		float angle = (float) Mth.atan2(target.getZ() - shaman.getZ(), target.getX() - shaman.getX());
		for (int i = 0; i < SPIKE_COUNT; i++) {
			double reach = SPIKE_SPACING * (i + 1);
			spawnSpike(level, shaman, shaman.getX() + Mth.cos(angle) * reach, shaman.getZ() + Mth.sin(angle) * reach,
					minY, maxY, angle, i);
		}
		level.playSound(null, shaman.getX(), shaman.getY(), shaman.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 0.7F);
	}

	/**
	 * Sangre de guerra: los orcos cercanos que están luchando reciben fuerza y regeneración.
	 *
	 * @return cuántos aliados fueron bendecidos (0 si no había nadie a quien ayudar)
	 */
	public static int warBlood(OrcEntity shaman) {
		if (!(shaman.level() instanceof ServerLevel level)) {
			return 0;
		}
		List<OrcEntity> allies = level.getEntitiesOfClass(OrcEntity.class, shaman.getBoundingBox().inflate(WAR_BLOOD_RADIUS),
				orc -> orc.isAlive() && orc.getTarget() != null);
		for (OrcEntity ally : allies) {
			ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, WAR_BLOOD_TICKS, 0), shaman);
			ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, WAR_BLOOD_TICKS / 2, 0), shaman);
			level.sendParticles(ParticleTypes.COPPER_FIRE_FLAME, ally.getX(), ally.getY(1.0), ally.getZ(), 8, 0.3, 0.4, 0.3, 0.01);
		}
		if (!allies.isEmpty()) {
			level.playSound(null, shaman.getX(), shaman.getY(), shaman.getZ(), SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.HOSTILE,
					1.0F, 0.6F);
		}
		return allies.size();
	}

	/** Busca el suelo entre {@code minY} y {@code maxY} y hace brotar allí una garra (como el evocador). */
	private static void spawnSpike(ServerLevel level, OrcEntity shaman, double x, double z, double minY, double maxY,
			float angle, int delayTicks) {
		BlockPos pos = BlockPos.containing(x, maxY, z);
		do {
			BlockPos below = pos.below();
			if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
				double topOffset = 0.0;
				if (!level.isEmptyBlock(pos)) {
					BlockState state = level.getBlockState(pos);
					VoxelShape shape = state.getCollisionShape(level, pos);
					if (!shape.isEmpty()) {
						topOffset = shape.max(Direction.Axis.Y);
					}
				}
				level.addFreshEntity(new EvokerFangs(level, x, pos.getY() + topOffset, z, angle, delayTicks, shaman));
				return;
			}
			pos = pos.below();
		} while (pos.getY() >= Mth.floor(minY) - 1);
	}
}
