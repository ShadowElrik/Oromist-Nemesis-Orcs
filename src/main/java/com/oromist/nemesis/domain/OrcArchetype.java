package com.oromist.nemesis.domain;

/**
 * Estilo de combate de un orco, determinado por su arma principal.
 * La capa de entidades traduce cada arquetipo a un objeto concreto de Minecraft
 * (ver {@code OrcLoadouts}).
 */
public enum OrcArchetype {
	SWORDSMAN(false),
	AXEMAN(false),
	ARCHER(true),
	CROSSBOWMAN(true);

	private final boolean ranged;

	OrcArchetype(boolean ranged) {
		this.ranged = ranged;
	}

	public boolean isRanged() {
		return ranged;
	}

	public String translationKey() {
		return "archetype.oromist-npc." + name().toLowerCase(java.util.Locale.ROOT);
	}
}
