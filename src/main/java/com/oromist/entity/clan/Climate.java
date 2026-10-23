package com.oromist.entity.clan;

/**
 * Clima del lugar donde aparece un orco, a partir de la temperatura del bioma y de si llueve.
 * Java puro: la capa del juego solo aporta esos dos datos.
 */
public enum Climate {
	/** Nieve y montañas heladas (temperatura menor que 0.3). */
	COLD,
	/** Llanuras, bosques, colinas: lo normal. */
	TEMPERATE,
	/** Calor húmedo: selvas, pantanos, manglares (≥ 0.8 y con lluvia). */
	HUMID,
	/** Calor seco: desiertos, sabanas, badlands (≥ 1.0 y sin lluvia). */
	ARID;

	public static final float COLD_BELOW = 0.3f;
	public static final float WARM_FROM = 0.8f;
	public static final float HOT_FROM = 1.0f;

	/**
	 * @param temperature      temperatura base del bioma (vanilla: −0.7 en picos helados, 2.0 en el desierto)
	 * @param hasPrecipitation si en el bioma llueve o nieva
	 */
	public static Climate of(float temperature, boolean hasPrecipitation) {
		if (temperature < COLD_BELOW) {
			return COLD;
		}
		if (temperature >= HOT_FROM && !hasPrecipitation) {
			return ARID;
		}
		if (temperature >= WARM_FROM && hasPrecipitation) {
			return HUMID;
		}
		return TEMPERATE;
	}
}
