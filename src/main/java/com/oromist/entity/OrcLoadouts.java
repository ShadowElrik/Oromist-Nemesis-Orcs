package com.oromist.entity;

import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcRank;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Equipamiento de cada orco según su arquetipo y rango. Separado de la entidad para
 * que balancear armas/armaduras no requiera tocar la lógica de comportamiento.
 */
public final class OrcLoadouts {
	/** Probabilidad (en partes de 100) de que un orco aparezca con cada arquetipo. */
	private static final Map<OrcArchetype, Integer> SPAWN_WEIGHTS = new EnumMap<>(Map.of(
			OrcArchetype.SWORDSMAN, 30,
			OrcArchetype.AXEMAN, 22,
			OrcArchetype.ARCHER, 20,
			OrcArchetype.CROSSBOWMAN, 13,
			OrcArchetype.LANCER, 8,
			OrcArchetype.SHAMAN, 7));
	private static final int TOTAL_WEIGHT = SPAWN_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();

	/** Bastón del chamán: un objeto vanilla que se reconoce a simple vista. */
	public static final Item SHAMAN_STAFF = Items.BLAZE_ROD;
	/** Color de la túnica del chamán (verde musgo). */
	private static final int SHAMAN_ROBE_COLOR = 0x3F5F2A;

	private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private OrcLoadouts() {
	}

	/** Elige un arquetipo aleatorio con pesos (más soldados cuerpo a cuerpo que tiradores o chamanes). */
	public static OrcArchetype randomArchetype(RandomSource random) {
		int roll = random.nextInt(TOTAL_WEIGHT);
		for (Map.Entry<OrcArchetype, Integer> entry : SPAWN_WEIGHTS.entrySet()) {
			roll -= entry.getValue();
			if (roll < 0) {
				return entry.getKey();
			}
		}
		return OrcArchetype.SWORDSMAN;
	}

	public static void equip(OrcEntity orc, OrcArchetype archetype, OrcRank rank, RandomSource random) {
		// Se parte de cero para que cambiar de arquetipo no deje piezas del anterior.
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			orc.setItemSlot(slot, ItemStack.EMPTY);
		}
		orc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon(archetype, rank)));
		equipArmor(orc, rank, random);
		if (archetype == OrcArchetype.SHAMAN) {
			equipShamanRegalia(orc);
		}

		float weaponDrop = rank.isNamed() ? 0.25f : 0.085f;
		orc.setDropChance(EquipmentSlot.MAINHAND, weaponDrop);
		for (EquipmentSlot slot : ARMOR_SLOTS) {
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
			case LANCER -> switch (rank) {
				case GRUNT -> Items.STONE_SPEAR;
				case CAPTAIN -> Items.IRON_SPEAR;
				case WARCHIEF -> Items.DIAMOND_SPEAR;
			};
			case ARCHER -> Items.BOW;
			case CROSSBOWMAN -> Items.CROSSBOW;
			case SHAMAN -> SHAMAN_STAFF;
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

	/** El chamán lleva una calavera como máscara y una túnica de cuero teñida, en lugar de armadura. */
	private static void equipShamanRegalia(OrcEntity orc) {
		orc.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
		ItemStack robe = new ItemStack(Items.LEATHER_CHESTPLATE);
		robe.set(DataComponents.DYED_COLOR, new DyedItemColor(SHAMAN_ROBE_COLOR));
		orc.setItemSlot(EquipmentSlot.CHEST, robe);
	}
}
