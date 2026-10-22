package com.oromist.horde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcName;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HordeBoardTest {
	private final UUID steve = UUID.randomUUID();

	static OrcProfile orc(OrcRank rank, Set<OrcTrait> traits) {
		return new OrcProfile(UUID.randomUUID(), new OrcName("Ugluk", ""), OrcArchetype.AXEMAN, 1.0f, 0, traits,
				List.of(), rank.powerRequired(), 0, 0, true);
	}

	private Memory memory(MemoryType type, long time) {
		return new Memory(type, steve, "Steve", time, "minecraft:plains");
	}

	@Test
	void orcsAreGroupedByRankKeepingTheirOrder() {
		OrcProfile warchief = orc(OrcRank.WARCHIEF, Set.of());
		OrcProfile captainA = orc(OrcRank.CAPTAIN, Set.of());
		OrcProfile captainB = orc(OrcRank.CAPTAIN, Set.of());
		OrcProfile grunt = orc(OrcRank.GRUNT, Set.of());

		HordeBoard board = HordeBoard.of(steve, List.of(captainA, grunt, warchief, captainB), Optional.empty());

		assertEquals(List.of(warchief), board.section(HordeBoard.Section.WARCHIEFS).stream().map(HordeBoard.Entry::profile).toList());
		assertEquals(List.of(captainA, captainB), board.section(HordeBoard.Section.CAPTAINS).stream().map(HordeBoard.Entry::profile).toList());
		assertEquals(List.of(grunt), board.section(HordeBoard.Section.GRUNTS).stream().map(HordeBoard.Entry::profile).toList());
		assertFalse(board.isEmpty());
	}

	@Test
	void onlyTheNemesisIsMarked() {
		OrcProfile nemesis = orc(OrcRank.CAPTAIN, Set.of());
		OrcProfile other = orc(OrcRank.CAPTAIN, Set.of());

		HordeBoard board = HordeBoard.of(steve, List.of(nemesis, other), Optional.of(nemesis.id()));

		List<HordeBoard.Entry> captains = board.section(HordeBoard.Section.CAPTAINS);
		assertTrue(captains.get(0).nemesis());
		assertFalse(captains.get(1).nemesis());
	}

	@Test
	void entriesShowWhatThePlayerKnowsAndTheMostImportantMemories() {
		OrcProfile gorbag = orc(OrcRank.CAPTAIN, EnumSet.of(OrcTrait.BRUTE, OrcTrait.COWARD, OrcTrait.SWIFT));
		gorbag.revealTraitTo(steve, OrcTrait.COWARD);
		gorbag.remember(memory(MemoryType.FIRST_ENCOUNTER, 0));
		gorbag.remember(memory(MemoryType.AMBUSHED_PLAYER, 10));
		gorbag.remember(memory(MemoryType.WOUNDED_BY_PLAYER, 20));
		gorbag.remember(memory(MemoryType.PLAYER_FLED, 30));
		gorbag.remember(memory(MemoryType.KILLED_PLAYER, 40));
		gorbag.remember(memory(MemoryType.SURVIVED_DEATH, 50));

		HordeBoard.Entry entry = HordeBoard.of(steve, List.of(gorbag), Optional.empty()).section(HordeBoard.Section.CAPTAINS).get(0);

		assertEquals(Set.of(OrcTrait.COWARD), entry.knownTraits());
		assertEquals(2, entry.hiddenTraitCount());
		assertEquals(gorbag.grudgeAgainst(steve), entry.grudge());
		assertEquals(List.of(MemoryType.SURVIVED_DEATH, MemoryType.KILLED_PLAYER, MemoryType.PLAYER_FLED, MemoryType.WOUNDED_BY_PLAYER),
				entry.memories().stream().map(Memory::type).toList(),
				"los " + HordeBoard.MAX_MEMORIES_SHOWN + " recuerdos más importantes, sin el primer encuentro");
	}

	@Test
	void aBoardWithoutOrcsIsEmpty() {
		assertTrue(HordeBoard.of(steve, List.of(), Optional.empty()).isEmpty());
	}
}
