package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Estrategia de combate según el arma que empuña el orco.
 *
 * <p>Para soportar un arma nueva (lanzas, hachas arrojadizas...) basta con añadir una
 * implementación a {@link #DEFAULTS}; {@link OrcCombatController} no cambia.</p>
 */
public interface WeaponCombatStrategy {
	boolean supports(ItemStack weapon);

	Goal createGoal(OrcEntity orc);

	/** Orden de evaluación: la primera que acepte el arma gana; la cuerpo a cuerpo acepta cualquiera. */
	List<WeaponCombatStrategy> DEFAULTS = List.of(new Bow(), new Crossbow(), new Melee());

	/** Arco: dispara a distancia y retrocede si el objetivo se acerca. */
	final class Bow implements WeaponCombatStrategy {
		@Override
		public boolean supports(ItemStack weapon) {
			return weapon.is(Items.BOW);
		}

		@Override
		public Goal createGoal(OrcEntity orc) {
			return new RangedBowAttackGoal<>(orc, 1.0, 30, 16.0F);
		}
	}

	/** Ballesta: carga, apunta y dispara usando la lógica vanilla de los saqueadores. */
	final class Crossbow implements WeaponCombatStrategy {
		@Override
		public boolean supports(ItemStack weapon) {
			return weapon.is(Items.CROSSBOW);
		}

		@Override
		public Goal createGoal(OrcEntity orc) {
			return new RangedCrossbowAttackGoal<>(orc, 1.0, 12.0F);
		}
	}

	/** Espadas, hachas o lo que tenga en la mano: carga hacia el objetivo. */
	final class Melee implements WeaponCombatStrategy {
		@Override
		public boolean supports(ItemStack weapon) {
			return true;
		}

		@Override
		public Goal createGoal(OrcEntity orc) {
			return new MeleeAttackGoal(orc, 1.15, true) {
				@Override
				public void start() {
					super.start();
					orc.setAggressive(true);
				}

				@Override
				public void stop() {
					super.stop();
					orc.setAggressive(false);
				}
			};
		}
	}
}
