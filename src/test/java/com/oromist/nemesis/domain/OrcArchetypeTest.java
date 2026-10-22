package com.oromist.nemesis.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrcArchetypeTest {
	@Test
	void shamansFightFromAfarAndRidersOnHorseback() {
		assertTrue(OrcArchetype.SHAMAN.isSpellcaster());
		assertTrue(OrcArchetype.SHAMAN.isRanged(), "el chamán mantiene la distancia");
		assertFalse(OrcArchetype.SHAMAN.isMelee());

		assertTrue(OrcArchetype.LANCER.isMounted());
		assertFalse(OrcArchetype.LANCER.isMelee(), "los jinetes no flanquean a pie");
	}

	@Test
	void onlyFootSoldiersWithMeleeWeaponsFlank() {
		for (OrcArchetype archetype : OrcArchetype.values()) {
			boolean expected = archetype == OrcArchetype.SWORDSMAN || archetype == OrcArchetype.AXEMAN;
			assertEquals(expected, archetype.isMelee(), archetype.name());
		}
	}
}
