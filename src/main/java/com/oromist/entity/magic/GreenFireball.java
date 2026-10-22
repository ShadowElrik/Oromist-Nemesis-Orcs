package com.oromist.entity.magic;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcMounts;
import com.oromist.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * "Fuego verde" de los chamanes: una bola de fuego mágica que quema y hiere a su objetivo.
 *
 * <p>A diferencia de las bolas de fuego vanilla, <b>no prende fuego a los bloques</b>, así
 * que una emboscada en tu casa no la reduce a cenizas. Tampoco daña a otros orcos.</p>
 */
public class GreenFireball extends Fireball {
	private static final float DAMAGE = 5.0F;
	private static final float BURN_SECONDS = 3.0F;

	public GreenFireball(EntityType<? extends GreenFireball> type, Level level) {
		super(type, level);
		setItem(new ItemStack(Items.SLIME_BALL)); // Se dibuja como una esfera verde.
	}

	public GreenFireball(Level level, LivingEntity caster, Vec3 direction) {
		super(ModEntities.GREEN_FIREBALL, caster, direction, level);
		setItem(new ItemStack(Items.SLIME_BALL));
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && !(entity instanceof OrcEntity) && !OrcMounts.isOrcMount(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (level() instanceof ServerLevel serverLevel) {
			Entity target = hitResult.getEntity();
			DamageSource source = damageSources().indirectMagic(this, getOwner());
			if (target.hurtServer(serverLevel, source, DAMAGE)) {
				target.igniteForSeconds(BURN_SECONDS);
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (level() instanceof ServerLevel serverLevel) {
			Vec3 at = hitResult.getLocation();
			serverLevel.sendParticles(ParticleTypes.COPPER_FIRE_FLAME, at.x, at.y, at.z, 12, 0.3, 0.3, 0.3, 0.02);
			discard();
		}
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.COPPER_FIRE_FLAME; // Llama verde.
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}
}
