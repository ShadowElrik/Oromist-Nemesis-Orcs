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
 */
public record NemesisRules(
		float cheatDeathChanceCaptain,
		float cheatDeathChanceWarchief,
		int powerForKillingPlayer,
		int powerForSurvivingDeath,
		int powerForPlayerFleeing,
		int powerForAmbush) {

	public static NemesisRules defaults() {
		return new NemesisRules(0.35f, 0.5f, 3, 2, 1, 0);
	}

	public float cheatDeathChance(OrcRank rank) {
		return switch (rank) {
			case GRUNT -> 0f;
			case CAPTAIN -> cheatDeathChanceCaptain;
			case WARCHIEF -> cheatDeathChanceWarchief;
		};
	}
}
