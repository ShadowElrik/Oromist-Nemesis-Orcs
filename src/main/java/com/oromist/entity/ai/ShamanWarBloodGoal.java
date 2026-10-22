package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.magic.ShamanSpells;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * "Sangre de guerra": cada cierto tiempo, mientras hay combate, el chamán enfurece a los orcos
 * cercanos (fuerza y regeneración). No usa movimiento ni mirada, así que funciona a la vez que
 * su meta de combate.
 */
public class ShamanWarBloodGoal extends Goal {
	private static final int COOLDOWN_TICKS = 12 * 20;

	private final OrcEntity shaman;
	private int cooldown = COOLDOWN_TICKS / 2;

	public ShamanWarBloodGoal(OrcEntity shaman) {
		this.shaman = shaman;
		setFlags(EnumSet.noneOf(Flag.class));
	}

	@Override
	public boolean canUse() {
		if (!shaman.archetype().isSpellcaster() || shaman.getTarget() == null) {
			return false;
		}
		return --cooldown <= 0;
	}

	@Override
	public void start() {
		// Si no había nadie luchando cerca, se reintenta pronto en lugar de esperar el ciclo entero.
		cooldown = ShamanSpells.warBlood(shaman) > 0 ? COOLDOWN_TICKS : 40;
	}

	@Override
	public boolean canContinueToUse() {
		return false; // Es instantáneo.
	}
}
