package com.oromist.nemesis.domain;

import java.util.Locale;

/**
 * Papel de un orco en la Horda. Determina su forma de luchar; la capa de entidades
 * traduce cada arquetipo a armas e IA concretas (ver {@code OrcLoadouts} y
 * {@code WeaponCombatStrategy}).
 */
public enum OrcArchetype {
	SWORDSMAN(CombatStyle.MELEE),
	AXEMAN(CombatStyle.MELEE),
	ARCHER(CombatStyle.RANGED),
	CROSSBOWMAN(CombatStyle.RANGED),
	/** Guardián de los huesos y la sangre: lanza fuego verde y fortalece a los suyos. */
	SHAMAN(CombatStyle.SPELLCASTER),
	/** Jinete de caballo zombi armado con lanza. */
	LANCER(CombatStyle.MOUNTED);

	private final CombatStyle style;

	OrcArchetype(CombatStyle style) {
		this.style = style;
	}

	public CombatStyle style() {
		return style;
	}

	/** ¿Prefiere luchar a distancia (arqueros, ballesteros y chamanes)? */
	public boolean isRanged() {
		return style == CombatStyle.RANGED || style == CombatStyle.SPELLCASTER;
	}

	/** ¿Combate cuerpo a cuerpo a pie? Solo estos orcos intentan flanquear. */
	public boolean isMelee() {
		return style == CombatStyle.MELEE;
	}

	public boolean isMounted() {
		return style == CombatStyle.MOUNTED;
	}

	public boolean isSpellcaster() {
		return style == CombatStyle.SPELLCASTER;
	}

	public String translationKey() {
		return "archetype.oromist-npc." + name().toLowerCase(Locale.ROOT);
	}

	/** Forma general de combatir, compartida por varios arquetipos. */
	public enum CombatStyle {
		MELEE,
		RANGED,
		SPELLCASTER,
		MOUNTED
	}
}
