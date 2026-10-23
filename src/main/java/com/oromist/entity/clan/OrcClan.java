package com.oromist.entity.clan;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Los clanes de la Horda y el clima en el que viven. Cada clan tiene 3 pieles consecutivas
 * (las genera {@code tools/orc_skins.py}, en este mismo orden).
 *
 * <p>Java puro, para poder probar el reparto de clanes sin arrancar el juego.</p>
 */
public enum OrcClan {
	/** Las 3 pieles originales (0–2): la Horda "de siempre", de tierras templadas. */
	FIRST_HORDE(0, Climate.TEMPERATE),
	BLOOD(3, Climate.ARID),
	PALE(6, Climate.COLD),
	SAND(9, Climate.ARID),
	TAR(12, Climate.HUMID),
	FROST(15, Climate.COLD),
	MUD(18, Climate.HUMID),
	MOSS(21, Climate.TEMPERATE),
	IRON(24, Climate.TEMPERATE);

	public static final int SKINS_PER_CLAN = 3;
	/** Probabilidad de que aparezca un orco de un clan de otro clima ("errante"). */
	public static final float WANDERER_CHANCE = 0.15f;

	private final int firstSkin;
	private final Climate climate;

	OrcClan(int firstSkin, Climate climate) {
		this.firstSkin = firstSkin;
		this.climate = climate;
	}

	public Climate climate() {
		return climate;
	}

	/** Número total de pieles (debe coincidir con {@code OrcEntity.SKIN_VARIANTS}). */
	public static int totalSkins() {
		return values().length * SKINS_PER_CLAN;
	}

	public static List<OrcClan> livingIn(Climate climate) {
		return Arrays.stream(values()).filter(clan -> clan.climate == climate).toList();
	}

	/** Clan al que pertenece una piel. */
	public static OrcClan ofSkin(int skin) {
		return values()[Math.floorMod(skin, totalSkins()) / SKINS_PER_CLAN];
	}

	/**
	 * Piel para un orco que aparece en ese clima: casi siempre de un clan local y, a veces,
	 * de cualquier clan (orcos errantes, para que nunca sea del todo predecible).
	 */
	public static int pickSkin(Climate climate, RandomGenerator random) {
		List<OrcClan> locals = livingIn(climate);
		boolean wanderer = locals.isEmpty() || random.nextFloat() < WANDERER_CHANCE;
		OrcClan clan = wanderer
				? values()[random.nextInt(values().length)]
				: locals.get(random.nextInt(locals.size()));
		return clan.firstSkin + random.nextInt(SKINS_PER_CLAN);
	}
}
