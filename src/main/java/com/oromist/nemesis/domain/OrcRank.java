package com.oromist.nemesis.domain;

/**
 * Jerarquía de los orcos dentro del ejército. Un orco asciende al acumular
 * "poder" (matar jugadores, sobrevivir a la muerte, etc.).
 *
 * <p>Cada rango define cuánto poder hace falta para alcanzarlo y un multiplicador
 * de estadísticas que la capa de entidades aplica sobre los atributos base.</p>
 */
public enum OrcRank {
	GRUNT(0, 1.0f, false),
	CAPTAIN(3, 1.5f, true),
	WARCHIEF(10, 2.2f, true);

	private final int powerRequired;
	private final float statMultiplier;
	private final boolean named;

	OrcRank(int powerRequired, float statMultiplier, boolean named) {
		this.powerRequired = powerRequired;
		this.statMultiplier = statMultiplier;
		this.named = named;
	}

	public int powerRequired() {
		return powerRequired;
	}

	public float statMultiplier() {
		return statMultiplier;
	}

	/** Los rangos con nombre muestran su nombre propio sobre la cabeza y en los diálogos. */
	public boolean isNamed() {
		return named;
	}

	/** Devuelve el rango más alto que corresponde a la cantidad de poder indicada. */
	public static OrcRank forPower(int power) {
		OrcRank result = GRUNT;
		for (OrcRank rank : values()) {
			if (power >= rank.powerRequired) {
				result = rank;
			}
		}
		return result;
	}
}
