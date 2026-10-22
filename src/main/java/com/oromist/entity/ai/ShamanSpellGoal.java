package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.magic.ShamanSpells;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Combate del chamán: se mantiene a media distancia lanzando fuego verde y, si alguien se le
 * acerca demasiado, hace brotar garras de hueso y retrocede. Nunca carga cuerpo a cuerpo.
 */
public class ShamanSpellGoal extends Goal {
	/** Por encima de esta distancia se acerca para poder lanzar. */
	private static final double MAX_CAST_DISTANCE = 18.0;
	/** Por debajo de esta distancia el objetivo está "encima" del chamán. */
	private static final double TOO_CLOSE = 5.0;
	private static final int FIRE_COOLDOWN = 50;
	private static final int SPIKES_COOLDOWN = 120;
	/** Cada cuántos ticks recalcula hacia dónde moverse (evita recalcular rutas sin parar). */
	private static final int REPATH_INTERVAL = 10;

	private final OrcEntity shaman;
	private int fireCooldown;
	private int spikesCooldown;
	private int repathCooldown;

	public ShamanSpellGoal(OrcEntity shaman) {
		this.shaman = shaman;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = shaman.getTarget();
		return target != null && target.isAlive();
	}

	@Override
	public void start() {
		shaman.setAggressive(true);
		fireCooldown = 20; // Un instante para "invocar" antes del primer hechizo.
	}

	@Override
	public void stop() {
		shaman.setAggressive(false);
		shaman.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = shaman.getTarget();
		if (target == null) {
			return;
		}
		shaman.getLookControl().setLookAt(target, 30.0F, 30.0F);
		double distance = shaman.distanceTo(target);
		boolean canSee = shaman.getSensing().hasLineOfSight(target);
		fireCooldown--;
		spikesCooldown--;
		repathCooldown--;

		if (distance < TOO_CLOSE && spikesCooldown <= 0) {
			ShamanSpells.boneSpikes(shaman, target);
			spikesCooldown = SPIKES_COOLDOWN;
		}
		move(target, distance, canSee);
		if (canSee && distance <= MAX_CAST_DISTANCE && fireCooldown <= 0) {
			ShamanSpells.greenFire(shaman, target);
			fireCooldown = FIRE_COOLDOWN + shaman.getRandom().nextInt(20);
		}
	}

	/** Acercarse si no lo ve o está lejos, retroceder si está encima, quedarse quieto si no. */
	private void move(LivingEntity target, double distance, boolean canSee) {
		if (repathCooldown > 0) {
			return;
		}
		repathCooldown = REPATH_INTERVAL;
		if (!canSee || distance > MAX_CAST_DISTANCE) {
			shaman.getNavigation().moveTo(target, 1.0);
		} else if (distance < TOO_CLOSE) {
			Vec3 away = LandRandomPos.getPosAway(shaman, 8, 4, target.position());
			if (away != null) {
				shaman.getNavigation().moveTo(away.x, away.y, away.z, 1.25);
			}
		} else {
			shaman.getNavigation().stop();
		}
	}
}
