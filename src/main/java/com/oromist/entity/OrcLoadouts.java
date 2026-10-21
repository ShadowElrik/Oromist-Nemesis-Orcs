package com.oromist.entity;

import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcRank;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Equipamiento de cada orco según su arquetipo y rango. Separado de la entidad para
 * que balancear armas/armaduras no requiera tocar la lógica de comportamiento.
 */
public final class OrcLoadouts {
	private OrcLoadouts() {
	}

	/** Elige un arquetipo aleatorio con pesos (más soldados cuerpo a cuerpo que tiradores). */
	public static OrcArchetype randomArchetype(RandomSource random) {
		int roll = random.nextInt(100);
		if (roll < 35) {
			return OrcArchetype.SWORDSMAN;
		} else if (roll < 60) {
			return OrcArchetype.AXEMAN;
		} else if (roll < 85) {
			return OrcArchetype.ARCHER;
		}
		return OrcArchetype.CROSSBOWMAN;
	}

	public static void equip(OrcEntity orc, OrcArchetype archetype, OrcRank rank, RandomSource random) {
		orc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon(archetype, rank)));
		equipArmor(orc, rank, random);

		float weaponDrop = rank.isNamed() ? 0.25f : 0.085f;
		orc.setDropChance(EquipmentSlot.MAINHAND, weaponDrop);
		for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
			orc.setDropChance(slot, 0.05f);
		}
	}

	private static Item weapon(OrcArchetype archetype, OrcRank rank) {
		return switch (archetype) {
			case SWORDSMAN -> switch (rank) {
				case GRUNT -> Items.STONE_SWORD;
				case CAPTAIN -> Items.IRON_SWORD;
				case WARCHIEF -> Items.DIAMOND_SWORD;
			};
			case AXEMAN -> switch (rank) {
				case GRUNT -> Items.STONE_AXE;
				case CAPTAIN -> Items.IRON_AXE;
				case WARCHIEF -> Items.DIAMOND_AXE;
			};
			case ARCHER -> Items.BOW;
			case CROSSBOWMAN -> Items.CROSSBOW;
		};
	}

	private static void equipArmor(OrcEntity orc, OrcRank rank, RandomSource random) {
		switch (rank) {
			case GRUNT -> {
				if (random.nextFloat() < 0.3f) {
					orc.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
				}
			}
			case CAPTAIN -> {
				orc.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
				orc.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
			}
			case WARCHIEF -> {
				orc.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
				orc.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
				orc.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
			}
		}
	}
}
