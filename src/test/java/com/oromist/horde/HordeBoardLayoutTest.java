package com.oromist.horde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HordeBoardLayoutTest {
	private final UUID steve = UUID.randomUUID();

	private HordeBoard boardWith(int warchiefs, int captains, int grunts) {
		List<OrcProfile> orcs = new ArrayList<>();
		for (int i = 0; i < warchiefs; i++) {
			orcs.add(HordeBoardTest.orc(OrcRank.WARCHIEF, Set.of()));
		}
		for (int i = 0; i < captains; i++) {
			orcs.add(HordeBoardTest.orc(OrcRank.CAPTAIN, Set.of()));
		}
		for (int i = 0; i < grunts; i++) {
			orcs.add(HordeBoardTest.orc(OrcRank.GRUNT, Set.of()));
		}
		return HordeBoard.of(steve, orcs, Optional.empty());
	}

	@Test
	void everySectionStartsWithItsLabelAndOrcsFollow() {
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(boardWith(1, 2, 1));

		assertEquals(HordeBoardLayout.SIZE, cells.size());
		assertEquals(new HordeBoardLayout.SectionLabel(HordeBoard.Section.WARCHIEFS, 1), cells.get(0));
		assertTrue(cells.get(1) instanceof HordeBoardLayout.OrcCell, "caudillo en la fila 0");
		assertEquals(new HordeBoardLayout.SectionLabel(HordeBoard.Section.CAPTAINS, 2), cells.get(9));
		assertTrue(cells.get(10) instanceof HordeBoardLayout.OrcCell);
		assertTrue(cells.get(11) instanceof HordeBoardLayout.OrcCell);
		assertTrue(cells.get(12) instanceof HordeBoardLayout.Empty);
		assertEquals(new HordeBoardLayout.SectionLabel(HordeBoard.Section.GRUNTS, 1), cells.get(27));
		assertTrue(cells.get(28) instanceof HordeBoardLayout.OrcCell);
		assertEquals(new HordeBoardLayout.Info(false), cells.get(HordeBoardLayout.INFO_SLOT));
	}

	@Test
	void captainsContinueOnTheirSecondRow() {
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(boardWith(0, 9, 0));

		assertTrue(cells.get(17) instanceof HordeBoardLayout.OrcCell, "octavo capitán: final de la fila 1");
		assertTrue(cells.get(18) instanceof HordeBoardLayout.Filler, "la columna de etiquetas no se usa para orcos");
		assertTrue(cells.get(19) instanceof HordeBoardLayout.OrcCell, "noveno capitán: inicio de la fila 2");
	}

	@Test
	void sectionsThatDoNotFitShowHowManyAreMissing() {
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(boardWith(10, 0, 0));

		long shown = cells.stream().filter(c -> c instanceof HordeBoardLayout.OrcCell).count();
		assertEquals(7L, shown, "8 casillas: 7 caudillos y el aviso");
		assertEquals(new HordeBoardLayout.Overflow(HordeBoard.Section.WARCHIEFS, 3), cells.get(8));
	}

	@Test
	void anEmptyBoardStillExplainsHowToFillIt() {
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(boardWith(0, 0, 0));

		assertEquals(new HordeBoardLayout.Info(true), cells.get(HordeBoardLayout.INFO_SLOT));
		assertTrue(cells.stream().noneMatch(c -> c instanceof HordeBoardLayout.OrcCell));
	}
}
