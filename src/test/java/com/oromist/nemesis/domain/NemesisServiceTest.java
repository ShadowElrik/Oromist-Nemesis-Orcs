package com.oromist.nemesis.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NemesisServiceTest {
	private static final long DAY = 24000L;
	private final UUID steve = UUID.randomUUID();
	private final UUID alex = UUID.randomUUID();
	private final EncounterContext steveInPlains = new EncounterContext(steve, "Steve", 3 * DAY, "minecraft:plains");
	private final List<String> events = new ArrayList<>();

	/** Reglas donde capitanes y caudillos siempre sobreviven, para que la prueba sea determinista. */
	private NemesisService service(float cheatDeathChance) {
		NemesisService service = new NemesisService(new InMemoryRepository(),
				new NemesisRules(cheatDeathChance, cheatDeathChance, 3, 2, 1, 0),
				new OrcNameGenerator.Syllabic(), new TraitRoller.Default(), new SplittableRandom(42));
		service.addListener(new NemesisListener() {
			@Override
			public void onPromoted(OrcProfile profile, OrcRank newRank) {
				events.add("promoted:" + newRank);
			}

			@Override
			public void onDied(OrcProfile profile, UUID playerId) {
				events.add("died");
			}
		});
		return service;
	}

	@Test
	void killingAPlayerPromotesAGruntToCaptain() {
		NemesisService service = service(1.0f);
		OrcProfile orc = service.createProfile(OrcArchetype.AXEMAN, 1.1f, 0, OrcRank.GRUNT);

		service.onOrcKilledPlayer(orc, steveInPlains);

		assertEquals(OrcRank.CAPTAIN, orc.rank());
		assertTrue(events.contains("promoted:CAPTAIN"), "se notifica el ascenso");
	}

	@Test
	void firstEncounterIsRecordedOnlyOnce() {
		NemesisService service = service(1.0f);
		OrcProfile orc = service.createProfile(OrcArchetype.ARCHER, 1.0f, 0, OrcRank.GRUNT);

		assertTrue(service.recordEncounter(orc, steveInPlains));
		assertFalse(service.recordEncounter(orc, steveInPlains));
	}

	@Test
	void nemesisIsTheOrcWithTheBiggestGrudgeThatIsAvailable() {
		NemesisService service = service(1.0f);
		OrcProfile killer = service.createProfile(OrcArchetype.SWORDSMAN, 1.0f, 0, OrcRank.GRUNT);
		OrcProfile witness = service.createProfile(OrcArchetype.SWORDSMAN, 1.0f, 0, OrcRank.GRUNT);
		service.onOrcKilledPlayer(killer, steveInPlains);
		service.onAllyKilledByPlayer(witness, steveInPlains);

		assertEquals(killer.id(), service.nemesisOf(steve, p -> true).orElseThrow().id());
		// Si el más rencoroso no está disponible (p. ej. ya está en el mundo), se elige el siguiente.
		assertEquals(witness.id(), service.nemesisOf(steve, p -> p != killer).orElseThrow().id());
		assertTrue(service.nemesisOf(alex, p -> true).isEmpty(), "Alex no tiene némesis");
	}

	@Test
	void captainsCanCheatDeathAndBecomeUndying() {
		NemesisService service = service(1.0f);
		OrcProfile captain = service.createProfile(OrcArchetype.CROSSBOWMAN, 1.2f, 0, OrcRank.CAPTAIN);

		NemesisService.DeathOutcome outcome = service.onOrcSlainByPlayer(captain, steveInPlains);

		assertEquals(NemesisService.DeathOutcome.CHEATED_DEATH, outcome);
		assertTrue(captain.isAlive());
		assertEquals(NemesisService.UNDYING_EPITHET, captain.name().epithetKey());
		assertEquals(MemoryType.SURVIVED_DEATH, captain.mostSignificantMemory(steve).orElseThrow().type());
	}

	@Test
	void gruntsAlwaysDieForGood() {
		NemesisService service = service(1.0f);
		OrcProfile grunt = service.createProfile(OrcArchetype.AXEMAN, 1.0f, 0, OrcRank.GRUNT);

		assertEquals(NemesisService.DeathOutcome.DIED, service.onOrcSlainByPlayer(grunt, steveInPlains));
		assertFalse(grunt.isAlive());
		assertTrue(events.contains("died"));
		assertTrue(service.enemiesOf(steve).isEmpty(), "los muertos no cuentan como enemigos");
	}

	@Test
	void incarnationsIncreaseSoOldEntitiesCanBeDetected() {
		NemesisService service = service(1.0f);
		OrcProfile orc = service.createProfile(OrcArchetype.AXEMAN, 1.0f, 0, OrcRank.CAPTAIN);

		int first = service.beginIncarnation(orc);
		int second = service.beginIncarnation(orc);

		assertEquals(first + 1, second);
		assertEquals(second, orc.incarnation());
	}

	@Test
	void memoryIsBoundedButKeepsTheMostImportantEvents() {
		NemesisService service = service(1.0f);
		OrcProfile orc = service.createProfile(OrcArchetype.AXEMAN, 1.0f, 0, OrcRank.GRUNT);
		service.onOrcKilledPlayer(orc, steveInPlains);
		for (int i = 0; i < 100; i++) {
			service.onWoundedByPlayer(orc, steveInPlains);
		}

		assertEquals(OrcProfile.MAX_MEMORIES, orc.memories().size());
		assertEquals(MemoryType.KILLED_PLAYER, orc.mostSignificantMemory(steve).orElseThrow().type());
	}

	@Test
	void appearanceIsKeptInTheProfile() {
		NemesisService service = service(1.0f);
		OrcProfile orc = service.createProfile(OrcArchetype.ARCHER, 0.9f, 2, OrcRank.GRUNT);

		assertEquals(2, orc.appearance());
	}

	@Test
	void losingAMountOrBeingInterrogatedIsRememberedWithAGrudge() {
		NemesisService service = service(1.0f);
		OrcProfile rider = service.createProfile(OrcArchetype.LANCER, 1.0f, 0, OrcRank.GRUNT);

		service.onMountKilled(rider, steveInPlains);
		service.onInterrogated(rider, steveInPlains);

		assertEquals(1L, rider.countMemories(steve, MemoryType.MOUNT_KILLED));
		assertEquals(1L, rider.countMemories(steve, MemoryType.INTERROGATED));
		assertEquals(MemoryType.INTERROGATED, rider.mostSignificantMemory(steve).orElseThrow().type(),
				"la humillación pesa más que perder la montura");
		assertTrue(service.nemesisOf(steve, p -> true).isPresent());
	}

	@Test
	void aShamanNearbyBringsCaptainsBackMoreOften() {
		int withoutShaman = survivors(false);
		int withShaman = survivors(true);

		assertTrue(withShaman > withoutShaman + 20, "con chamán: " + withShaman + ", sin chamán: " + withoutShaman);
	}

	/** De 100 capitanes "muertos", cuántos vuelven (10 % base, +50 % con chamán). */
	private int survivors(boolean shamanNearby) {
		NemesisService service = new NemesisService(new InMemoryRepository(), new NemesisRules(0.1f, 0.1f, 3, 2, 1, 0, 0.5f),
				new OrcNameGenerator.Syllabic(), new TraitRoller.Default(), new SplittableRandom(1));
		int survived = 0;
		for (int i = 0; i < 100; i++) {
			OrcProfile captain = service.createProfile(OrcArchetype.SHAMAN, 1.0f, 0, OrcRank.CAPTAIN);
			if (service.onOrcSlainByPlayer(captain, steveInPlains, shamanNearby) == NemesisService.DeathOutcome.CHEATED_DEATH) {
				survived++;
			}
		}
		return survived;
	}
}
