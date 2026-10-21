package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Un orco malherido que decide huir corre lejos de su atacante. Si consigue alejarse
 * lo suficiente "escapa": desaparece y queda registrado en su memoria, listo para
 * volver más adelante en busca de venganza.
 */
public final class OrcRetreatGoal extends Goal {
	private static final double ESCAPE_DISTANCE = 28.0;
	private static final int MAX_RETREAT_TICKS = 20 * 20;
	private static final double WATCH_RADIUS = 16.0;

	private final OrcEntity orc;
	private final double speed;
	private LivingEntity threat;
	private int ticks;

	public OrcRetreatGoal(OrcEntity orc, double speed) {
		this.orc = orc;
		this.speed = speed;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = orc.getTarget();
		if (target == null || !orc.wantsToRetreat()) {
			return false;
		}
		threat = target;
		return true;
	}

	@Override
	public void start() {
		ticks = 0;
		orc.onStartRetreat(threat);
		moveAway();
	}

	@Override
	public boolean canContinueToUse() {
		return threat != null && threat.isAlive() && ticks < MAX_RETREAT_TICKS;
	}

	@Override
	public void tick() {
		ticks++;
		if (orc.distanceTo(threat) > ESCAPE_DISTANCE && !isWatchedByAnyPlayer()) {
			orc.escapeFrom(threat);
			threat = null;
			return;
		}
		if (orc.getNavigation().isDone() || ticks % 20 == 0) {
			moveAway();
		}
	}

	@Override
	public void stop() {
		threat = null;
	}

	/**
	 * En multijugador el orco solo "escapa" (desaparece) si ningún jugador está cerca,
	 * para que nadie lo vea esfumarse delante de sus ojos.
	 */
	private boolean isWatchedByAnyPlayer() {
		return orc.level().getNearestPlayer(orc, WATCH_RADIUS) != null;
	}

	private void moveAway() {
		Vec3 away = DefaultRandomPos.getPosAway(orc, 16, 7, threat.position());
		if (away != null) {
			orc.getNavigation().moveTo(away.x, away.y, away.z, speed);
		}
	}
}
