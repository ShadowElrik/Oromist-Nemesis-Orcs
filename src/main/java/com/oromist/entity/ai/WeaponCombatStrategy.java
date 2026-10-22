package com.oromist.entity.ai;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcLoadouts;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.SpearUseGoal;
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
	List<WeaponCombatStrategy> DEFAULTS = List.of(new Bow(), new Crossbow(), new Staff(), new Spear(), new Melee());

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

	/** Bastón de chamán: lanza hechizos a distancia (ver {@link ShamanSpellGoal}). */
	final class Staff implements WeaponCombatStrategy {
		@Override
		public boolean supports(ItemStack weapon) {
			return weapon.is(OrcLoadouts.SHAMAN_STAFF);
		}

		@Override
		public Goal createGoal(OrcEntity orc) {
			return new ShamanSpellGoal(orc);
		}
	}

	/**
	 * Lanzas: embiste, se aleja y vuelve a embestir, igual que los zombis jinetes vanilla.
	 * Funciona a pie y, sobre todo, a lomos de un caballo zombi.
	 */
	final class Spear implements WeaponCombatStrategy {
		@Override
		public boolean supports(ItemStack weapon) {
			return weapon.has(DataComponents.KINETIC_WEAPON);
		}

		@Override
		public Goal createGoal(OrcEntity orc) {
			return new SpearUseGoal<>(orc, 1.0, 1.0, 10.0F, 2.0F);
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
