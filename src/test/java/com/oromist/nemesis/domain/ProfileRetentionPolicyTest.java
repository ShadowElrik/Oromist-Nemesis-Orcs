package com.oromist.nemesis.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfileRetentionPolicyTest {
	private static final UUID PLAYER = UUID.randomUUID();
	private final ProfileRetentionPolicy policy = new ProfileRetentionPolicy(5, 10);

	private static OrcProfile orc(OrcRank rank, boolean alive, long memoryTime, MemoryType memory) {
		return new OrcProfile(UUID.randomUUID(), new OrcName("Ug", ""), OrcArchetype.AXEMAN, 1.0f, 0, Set.of(),
				List.of(new Memory(memory, PLAYER, "Steve", memoryTime, "")), rank.powerRequired(), 0, 0, alive);
	}

	@Test
	void nothingIsRemovedUnderTheLimits() {
		List<OrcProfile> profiles = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			profiles.add(orc(OrcRank.GRUNT, true, i, MemoryType.FIRST_ENCOUNTER));
		}
		assertTrue(policy.profilesToRemove(profiles).isEmpty());
	}

	/** En un servidor grande los perfiles no deben crecer sin límite, pero los capitanes nunca se pierden. */
	@Test
	void capsGruntsAndDeadButNeverRemovesNamedOrcs() {
		List<OrcProfile> profiles = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			profiles.add(orc(OrcRank.GRUNT, true, i, MemoryType.FIRST_ENCOUNTER));
		}
		for (int i = 0; i < 8; i++) {
			profiles.add(orc(OrcRank.GRUNT, false, i, MemoryType.FIRST_ENCOUNTER));
		}
		List<OrcProfile> captains = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			captains.add(orc(OrcRank.CAPTAIN, true, 0, MemoryType.FIRST_ENCOUNTER));
		}
		profiles.addAll(captains);

		List<UUID> removed = policy.profilesToRemove(profiles);

		assertEquals(5 + 3, removed.size(), "5 soldados sobrantes y 3 muertos sobrantes");
		for (OrcProfile captain : captains) {
			assertFalse(removed.contains(captain.id()), "un capitán no debe purgarse");
		}
	}

	@Test
	void gruntsWithMoreGrudgeAndRecentMemoriesAreKept() {
		List<OrcProfile> profiles = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			profiles.add(orc(OrcRank.GRUNT, true, i, MemoryType.FIRST_ENCOUNTER));
		}
		OrcProfile oldButHateful = orc(OrcRank.GRUNT, true, 0, MemoryType.PLAYER_FLED);
		profiles.add(oldButHateful);

		List<UUID> removed = policy.profilesToRemove(profiles);

		assertEquals(1, removed.size());
		assertEquals(profiles.get(0).id(), removed.get(0), "se descarta el de menos rencor y recuerdo más antiguo");
		assertFalse(removed.contains(oldButHateful.id()));
	}
}
