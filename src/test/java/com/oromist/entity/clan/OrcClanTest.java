package com.oromist.entity.clan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oromist.entity.OrcEntity;
import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class OrcClanTest {
	@Test
	void vanillaBiomesGetTheExpectedClimate() {
		assertEquals(Climate.COLD, Climate.of(0.0f, true), "llanura nevada");
		assertEquals(Climate.COLD, Climate.of(-0.7f, true), "picos helados");
		assertEquals(Climate.ARID, Climate.of(2.0f, false), "desierto, sabana y badlands");
		assertEquals(Climate.HUMID, Climate.of(0.95f, true), "selva");
		assertEquals(Climate.HUMID, Climate.of(0.8f, true), "pantano");
		assertEquals(Climate.TEMPERATE, Climate.of(0.8f, false), "llanura (sin calor húmedo)");
		assertEquals(Climate.TEMPERATE, Climate.of(0.7f, true), "bosque");
		assertEquals(Climate.COLD, Climate.of(0.25f, true), "taiga");
		assertEquals(Climate.TEMPERATE, Climate.of(0.3f, true), "taiga de pinos viejos");
	}

	@Test
	void everyClimateHasItsOwnClans() {
		for (Climate climate : Climate.values()) {
			assertFalse(OrcClan.livingIn(climate).isEmpty(), "ningún clan vive en " + climate);
		}
	}

	@Test
	void thereIsATextureForEverySkinOfEveryClan() {
		assertEquals(OrcEntity.SKIN_VARIANTS, OrcClan.totalSkins());
	}

	@Test
	void orcsAreMostlyFromLocalClansButSomeWander() {
		SplittableRandom random = new SplittableRandom(11);
		Map<Climate, Integer> local = new EnumMap<>(Climate.class);
		int samples = 2000;
		for (int i = 0; i < samples; i++) {
			int skin = OrcClan.pickSkin(Climate.COLD, random);
			assertTrue(skin >= 0 && skin < OrcClan.totalSkins(), "piel fuera de rango: " + skin);
			local.merge(OrcClan.ofSkin(skin).climate(), 1, Integer::sum);
		}
		int cold = local.getOrDefault(Climate.COLD, 0);
		assertTrue(cold > samples * 0.8, "la mayoría deben ser de clanes del frío: " + cold);
		assertTrue(cold < samples, "y alguno debe ser un errante de otro clima");
	}

	@Test
	void skinsMapBackToTheirClan() {
		assertEquals(OrcClan.FIRST_HORDE, OrcClan.ofSkin(2));
		assertEquals(OrcClan.BLOOD, OrcClan.ofSkin(3));
		assertEquals(OrcClan.FROST, OrcClan.ofSkin(17));
		assertEquals(OrcClan.IRON, OrcClan.ofSkin(26));
	}
}
