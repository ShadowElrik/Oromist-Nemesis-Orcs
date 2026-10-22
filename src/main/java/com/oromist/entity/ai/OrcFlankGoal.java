package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Flanqueo: cuando otro orco ya está atacando de frente al mismo objetivo, este no se pone en
 * fila detrás, sino que rodea al objetivo para atacarlo por el lado contrario.
 *
 * <p>Solo lo usan los orcos cuerpo a cuerpo a pie. Dura unos segundos; después la meta de
 * combate normal (prioridad menor) se encarga de golpear.</p>
 */
public class OrcFlankGoal extends Goal {
	private static final double ALLY_SEARCH_RADIUS = 16.0;
	/** Solo flanquea si está a una distancia "intermedia" del objetivo. */
	private static final double MIN_DISTANCE = 4.0;
	private static final double MAX_DISTANCE = 16.0;
	/** A cuántos bloques del objetivo, por el lado opuesto al aliado, se coloca. */
	private static final double FLANK_OFFSET = 3.0;
	private static final int MAX_DURATION = 60;
	private static final int COOLDOWN = 200;
	private static final double SPEED = 1.25;

	private final OrcEntity orc;
	private Vec3 flankPoint;
	private int ticksLeft;
	private int cooldown;

	public OrcFlankGoal(OrcEntity orc) {
		this.orc = orc;
		setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (cooldown > 0) {
			cooldown--;
			return false;
		}
		LivingEntity target = orc.getTarget();
		if (target == null || !orc.archetype().isMelee() || orc.isPassenger()) {
			return false;
		}
		double distance = orc.distanceTo(target);
		if (distance < MIN_DISTANCE || distance > MAX_DISTANCE) {
			return false;
		}
		Optional<OrcEntity> frontliner = closestAllyAttacking(target);
		if (frontliner.isEmpty() || frontliner.get().distanceToSqr(target) >= orc.distanceToSqr(target)) {
			return false; // Nadie va delante: este orco es el que ataca de frente.
		}
		flankPoint = oppositeSide(target, frontliner.get());
		return true;
	}

	@Override
	public void start() {
		ticksLeft = MAX_DURATION;
		orc.getNavigation().moveTo(flankPoint.x, flankPoint.y, flankPoint.z, SPEED);
	}

	@Override
	public boolean canContinueToUse() {
		return ticksLeft > 0 && orc.getTarget() != null && !orc.getNavigation().isDone()
				&& orc.position().distanceToSqr(flankPoint) > 1.5 * 1.5;
	}

	@Override
	public void tick() {
		ticksLeft--;
		if (orc.getTarget() != null) {
			orc.getLookControl().setLookAt(orc.getTarget(), 30.0F, 30.0F);
		}
	}

	@Override
	public void stop() {
		cooldown = COOLDOWN;
		flankPoint = null;
	}

	/** El aliado más cercano al objetivo que también lo está atacando. */
	private Optional<OrcEntity> closestAllyAttacking(LivingEntity target) {
		return orc.level().getEntitiesOfClass(OrcEntity.class, orc.getBoundingBox().inflate(ALLY_SEARCH_RADIUS),
						ally -> ally != orc && ally.isAlive() && ally.getTarget() == target)
				.stream()
				.min(Comparator.comparingDouble(ally -> ally.distanceToSqr(target)));
	}

	/** Punto al otro lado del objetivo respecto al aliado: si el aliado ataca por delante, este va por detrás. */
	private static Vec3 oppositeSide(LivingEntity target, OrcEntity ally) {
		Vec3 fromAlly = target.position().subtract(ally.position());
		Vec3 horizontal = new Vec3(fromAlly.x, 0, fromAlly.z);
		if (horizontal.lengthSqr() < 1.0E-4) {
			horizontal = new Vec3(1, 0, 0);
		}
		return target.position().add(horizontal.normalize().scale(FLANK_OFFSET));
	}
}
