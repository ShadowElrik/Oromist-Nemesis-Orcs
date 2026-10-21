package com.oromist.nemesis.domain;

/**
 * Rasgos de personalidad/combate de un orco. Son datos puros: el efecto concreto
 * de cada rasgo lo implementa la capa de entidades (ver {@code TraitEffects}),
 * así añadir un rasgo no obliga a modificar el dominio (principio abierto/cerrado).
 */
public enum OrcTrait {
	/** Golpea más fuerte. */
	BRUTE(TraitKind.STRENGTH),
	/** Se mueve más rápido. */
	SWIFT(TraitKind.STRENGTH),
	/** Más vida y armadura. */
	THICK_HIDE(TraitKind.STRENGTH),
	/** Las flechas apenas le hacen daño. */
	ARROW_WARD(TraitKind.STRENGTH),
	/** Se enfurece cuando le queda poca vida. */
	BERSERKER(TraitKind.STRENGTH),
	/** Recibe el doble de daño por fuego. */
	FEARS_FIRE(TraitKind.WEAKNESS),
	/** Huye cuando está malherido (y así sobrevive para vengarse). */
	COWARD(TraitKind.WEAKNESS),
	/** Recibe más daño cuerpo a cuerpo. */
	FRAIL(TraitKind.WEAKNESS);

	private final TraitKind kind;

	OrcTrait(TraitKind kind) {
		this.kind = kind;
	}

	public TraitKind kind() {
		return kind;
	}

	public String translationKey() {
		return "trait.oromist-npc." + name().toLowerCase(java.util.Locale.ROOT);
	}

	public enum TraitKind {
		STRENGTH,
		WEAKNESS
	}
}
