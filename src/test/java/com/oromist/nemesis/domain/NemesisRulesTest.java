package com.oromist.nemesis.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NemesisRulesTest {
	private static final float DELTA = 1.0E-6f;
	private final NemesisRules rules = new NemesisRules(0.35f, 0.5f, 3, 2, 1, 0, 0.2f);

	@Test
	void aShamanNearbyMakesCaptainsMoreLikelyToReturn() {
		assertEquals(0.35f, rules.cheatDeathChance(OrcRank.CAPTAIN, false), DELTA);
		assertEquals(0.55f, rules.cheatDeathChance(OrcRank.CAPTAIN, true), DELTA);
		assertEquals(0.7f, rules.cheatDeathChance(OrcRank.WARCHIEF, true), DELTA);
	}

	@Test
	void gruntsNeverReturnEvenWithAShaman() {
		assertEquals(0f, rules.cheatDeathChance(OrcRank.GRUNT, true), DELTA);
	}

	@Test
	void theShamanBonusIsCappedButNeverLowersTheConfiguredChance() {
		NemesisRules generous = new NemesisRules(0.8f, 0.95f, 3, 2, 1, 0, 0.5f);

		assertEquals(NemesisRules.MAX_CHEAT_DEATH_CHANCE, generous.cheatDeathChance(OrcRank.CAPTAIN, true), DELTA);
		// Si el servidor configuró 95 %, el chamán no lo baja al tope.
		assertEquals(0.95f, generous.cheatDeathChance(OrcRank.WARCHIEF, true), DELTA);
	}

	@Test
	void rulesWithoutShamanBonusBehaveAsBefore() {
		NemesisRules legacy = new NemesisRules(0.35f, 0.5f, 3, 2, 1, 0);

		assertEquals(0.35f, legacy.cheatDeathChance(OrcRank.CAPTAIN, true), DELTA);
	}
}
