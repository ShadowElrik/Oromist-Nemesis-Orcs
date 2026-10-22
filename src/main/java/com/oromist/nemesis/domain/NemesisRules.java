package com.oromist.nemesis.domain;

/**
 * Parámetros de balance del sistema Némesis. Es un valor inmutable que se
 * construye desde la configuración del mod.
 *
 * @param cheatDeathChanceCaptain  probabilidad de que un capitán sobreviva a su "muerte"
 * @param cheatDeathChanceWarchief probabilidad para un caudillo
 * @param powerForKillingPlayer    poder ganado al matar a un jugador
 * @param powerForSurvivingDeath   poder ganado al sobrevivir a la muerte
 * @param powerForPlayerFleeing    poder ganado cuando el jugador huye
 * @param powerForAmbush           poder ganado al emboscar (aunque no mate)
 * @param shamanCheatDeathBonus    probabilidad extra de volver si un chamán estaba cerca al morir
 */
public record NemesisRules(
		float cheatDeathChanceCaptain,
		float cheatDeathChanceWarchief,
		int powerForKillingPlayer,
		int powerForSurvivingDeath,
		int powerForPlayerFleeing,
		int powerForAmbush,
		float shamanCheatDeathBonus) {

	/** Nunca es seguro al 100 % volver de la muerte, por muchos chamanes que haya. */
	public static final float MAX_CHEAT_DEATH_CHANCE = 0.9f;

	/** Reglas sin bonificación de chamán (compatibilidad con la configuración anterior). */
	public NemesisRules(float cheatDeathChanceCaptain, float cheatDeathChanceWarchief, int powerForKillingPlayer,
			int powerForSurvivingDeath, int powerForPlayerFleeing, int powerForAmbush) {
		this(cheatDeathChanceCaptain, cheatDeathChanceWarchief, powerForKillingPlayer, powerForSurvivingDeath,
				powerForPlayerFleeing, powerForAmbush, 0f);
	}

	public static NemesisRules defaults() {
		return new NemesisRules(0.35f, 0.5f, 3, 2, 1, 0, 0.2f);
	}

	public float cheatDeathChance(OrcRank rank) {
		return switch (rank) {
			case GRUNT -> 0f;
			case CAPTAIN -> cheatDeathChanceCaptain;
			case WARCHIEF -> cheatDeathChanceWarchief;
		};
	}

	/**
	 * Probabilidad de engañar a la muerte teniendo en cuenta a los chamanes.
	 * Los soldados rasos nunca vuelven: el ritual solo se gasta en los que tienen nombre.
	 */
	public float cheatDeathChance(OrcRank rank, boolean shamanNearby) {
		float base = cheatDeathChance(rank);
		if (base <= 0f || !shamanNearby) {
			return base;
		}
		// Una configuración base muy alta se respeta tal cual; el chamán nunca la reduce.
		return Math.max(base, Math.min(MAX_CHEAT_DEATH_CHANCE, base + shamanCheatDeathBonus));
	}
}
