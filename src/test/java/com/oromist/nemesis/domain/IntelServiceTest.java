package com.oromist.nemesis.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntelServiceTest {
	private final UUID steve = UUID.randomUUID();
	private final UUID alex = UUID.randomUUID();
	private final InMemoryRepository repository = new InMemoryRepository();
	private final IntelService intel = new IntelService(repository, new SplittableRandom(7));

	private OrcProfile orc(OrcRank rank, Set<OrcTrait> traits) {
		OrcProfile profile = new OrcProfile(UUID.randomUUID(), new OrcName("Ugluk", ""), OrcArchetype.AXEMAN, 1.0f, 0,
				traits, List.of(), rank.powerRequired(), 0, 0, true);
		repository.save(profile);
		return profile;
	}

	private void makeEnemyOf(OrcProfile profile, UUID player) {
		profile.remember(new Memory(MemoryType.KILLED_PLAYER, player, "Steve", 0L, "minecraft:plains"));
	}

	// --- Rasgos conocidos por cada jugador ---------------------------------------------

	@Test
	void traitsStartHiddenAndAreRevealedPerPlayer() {
		OrcProfile gorbag = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE, OrcTrait.FEARS_FIRE));

		assertEquals(EnumSet.of(OrcTrait.BRUTE, OrcTrait.FEARS_FIRE), gorbag.hiddenTraits(steve));
		assertTrue(intel.reveal(gorbag, steve, OrcTrait.FEARS_FIRE));

		assertEquals(Set.of(OrcTrait.FEARS_FIRE), gorbag.knownTraits(steve));
		assertEquals(Set.of(OrcTrait.BRUTE), gorbag.hiddenTraits(steve));
		assertTrue(gorbag.knownTraits(alex).isEmpty(), "lo que sabe Steve no lo sabe Alex");
	}

	@Test
	void revealingTwiceOrRevealingATraitTheOrcDoesNotHaveChangesNothing() {
		OrcProfile gorbag = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE));

		assertTrue(intel.reveal(gorbag, steve, OrcTrait.BRUTE));
		int changes = repository.changes();
		assertFalse(intel.reveal(gorbag, steve, OrcTrait.BRUTE), "ya lo sabía");
		assertFalse(intel.reveal(gorbag, steve, OrcTrait.COWARD), "no tiene ese rasgo");
		assertEquals(changes, repository.changes(), "no hay nada nuevo que guardar");
	}

	// --- Interrogatorios y notas de guerra ----------------------------------------------

	@Test
	void anInformantTalksAboutTheNemesisAndPrefersWeaknesses() {
		OrcProfile nemesis = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE, OrcTrait.THICK_HIDE, OrcTrait.FEARS_FIRE));
		makeEnemyOf(nemesis, steve);
		OrcProfile informant = orc(OrcRank.GRUNT, EnumSet.of(OrcTrait.COWARD));
		makeEnemyOf(informant, steve);

		Optional<IntelService.Intel> secret = intel.extractIntel(steve, Optional.of(informant.id()));

		assertTrue(secret.isPresent());
		assertEquals(nemesis.id(), secret.get().subject().id(), "habla del némesis, no de sí mismo");
		assertEquals(OrcTrait.FEARS_FIRE, secret.get().trait(), "lo más útil es una debilidad");
		assertTrue(nemesis.knownTraits(steve).contains(OrcTrait.FEARS_FIRE));
	}

	@Test
	void anonymousGruntsThatDoNotKnowThePlayerAreNotWorthTalkingAbout() {
		orc(OrcRank.GRUNT, EnumSet.of(OrcTrait.FRAIL));

		assertTrue(intel.extractIntel(steve).isEmpty());
	}

	@Test
	void notesCanRevealCaptainsThePlayerHasNotMetYet() {
		OrcProfile stranger = orc(OrcRank.WARCHIEF, EnumSet.of(OrcTrait.SWIFT));

		Optional<IntelService.Intel> secret = intel.extractIntel(steve);

		assertEquals(stranger.id(), secret.orElseThrow().subject().id());
		assertEquals(OrcTrait.SWIFT, secret.get().trait());
	}

	@Test
	void whenEverythingIsKnownThereIsNothingLeftToTell() {
		OrcProfile captain = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE, OrcTrait.COWARD));

		assertTrue(intel.extractIntel(steve).isPresent());
		assertTrue(intel.extractIntel(steve).isPresent());
		assertTrue(intel.extractIntel(steve).isEmpty(), "ya conoce los dos rasgos");
		assertEquals(captain.traits(), captain.knownTraits(steve));
	}

	@Test
	void deadOrcsAreNeverTheSubject() {
		OrcProfile dead = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE));
		dead.markDead();

		assertTrue(intel.extractIntel(steve).isEmpty());
	}
}
