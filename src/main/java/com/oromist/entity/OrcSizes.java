package com.oromist.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Distribución de alturas de los orcos: la mayoría ronda la altura de un jugador,
 * pero aparecen ejemplares bajos y algunos gigantes.
 */
public final class OrcSizes {
	private static float min = 0.85f;
	private static float max = 1.35f;

	private OrcSizes() {
	}

	public static void configure(float minScale, float maxScale) {
		min = Math.min(minScale, maxScale);
		max = Math.max(minScale, maxScale);
	}

	public static float randomScale(RandomSource random) {
		// Campana centrada un poco por encima de 1.0 (los orcos son algo más altos que un humano).
		float center = 1.05f;
		float spread = (max - min) / 4.0f;
		float value = center + (float) random.nextGaussian() * spread;
		return Mth.clamp(value, min, max);
	}
}
