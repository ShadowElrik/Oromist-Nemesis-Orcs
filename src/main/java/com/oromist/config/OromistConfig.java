package com.oromist.config;

import com.oromist.nemesis.domain.NemesisRules;

/**
 * Configuración del mod (archivo {@code config/oromist-npc.json}).
 * Es un objeto plano para que Gson lo lea/escriba sin código extra; los valores por
 * defecto están en los inicializadores de campo.
 */
public final class OromistConfig {
	// --- Némesis ---
	public float cheatDeathChanceCaptain = 0.35f;
	public float cheatDeathChanceWarchief = 0.5f;
	public int powerForKillingPlayer = 3;
	public int powerForSurvivingDeath = 2;
	public int powerForPlayerFleeing = 1;
	/** Radio en bloques en el que los jugadores leen lo que dice un orco. */
	public int dialogueRadius = 32;
	/** Segundos mínimos entre dos frases del mismo orco al mismo jugador. */
	public int dialogueCooldownSeconds = 90;

	// --- Emboscadas ---
	public boolean ambushesEnabled = true;
	/** Cada cuántos segundos se evalúa si ocurre una emboscada para cada jugador. */
	public int ambushCheckIntervalSeconds = 60;
	/** Tiempo mínimo (minutos de juego real) entre emboscadas al mismo jugador. */
	public int ambushCooldownMinutes = 15;
	/** Probabilidad por evaluación según la actividad del jugador. */
	public float ambushChanceAtHome = 0.04f;
	public float ambushChanceMining = 0.06f;
	public float ambushChanceTraveling = 0.08f;
	/** Probabilidad de que el némesis del jugador encabece la emboscada. */
	public float nemesisLeadsAmbushChance = 0.7f;
	public int ambushMinGrunts = 2;
	public int ambushMaxGrunts = 4;
	/** Minutos de juego que debe acumular cada jugador antes de su primera emboscada (protege a los recién llegados). */
	public int ambushGraceMinutes = 30;
	/** No hay emboscada si ya hay este número de orcos cerca del jugador (evita acumulaciones en grupo). */
	public int ambushMaxNearbyOrcs = 8;
	/** Dimensiones donde pueden ocurrir emboscadas. */
	public java.util.List<String> ambushDimensions = java.util.List.of("minecraft:overworld");

	// --- Orcos ---
	public float minOrcScale = 0.85f;
	public float maxOrcScale = 1.35f;

	public NemesisRules toRules() {
		return new NemesisRules(cheatDeathChanceCaptain, cheatDeathChanceWarchief, powerForKillingPlayer,
				powerForSurvivingDeath, powerForPlayerFleeing, 0);
	}
}
