package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * Mantiene activo el objetivo de combate adecuado al arma actual del orco.
 * Si el orco cambia de arma (la recoge del suelo, se la equipan...) se re-evalúa.
 */
public final class OrcCombatController {
	private static final int GOAL_PRIORITY = 4;

	private final OrcEntity orc;
	private final List<WeaponCombatStrategy> strategies;
	private Goal activeGoal;
	private WeaponCombatStrategy activeStrategy;

	public OrcCombatController(OrcEntity orc, List<WeaponCombatStrategy> strategies) {
		this.orc = orc;
		this.strategies = List.copyOf(strategies);
	}

	public void reassess() {
		if (orc.level().isClientSide()) {
			return;
		}
		ItemStack weapon = orc.getItemInHand(InteractionHand.MAIN_HAND);
		WeaponCombatStrategy strategy = strategies.stream()
				.filter(s -> s.supports(weapon))
				.findFirst()
				.orElseThrow();
		if (strategy == activeStrategy) {
			return;
		}
		if (activeGoal != null) {
			orc.goalSelector().removeGoal(activeGoal);
		}
		activeStrategy = strategy;
		activeGoal = strategy.createGoal(orc);
		orc.goalSelector().addGoal(GOAL_PRIORITY, activeGoal);
	}
}
