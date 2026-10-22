package com.oromist.horde;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Dónde va cada cosa en el menú del tablero (un cofre doble de 6 filas × 9 columnas):
 *
 * <pre>
 * fila 0   [Caudillos] o o o o o o o o
 * fila 1   [Capitanes] o o o o o o o o
 * fila 2   [  ·  ]     o o o o o o o o
 * fila 3   [Soldados]  o o o o o o o o
 * fila 4   [  ·  ]     o o o o o o o o
 * fila 5    · · · · [Ayuda] · · · ·
 * </pre>
 *
 * <p>Si un grupo no cabe, su última casilla muestra "+N más". Es Java puro y se prueba sin el juego.</p>
 */
public final class HordeBoardLayout {
	public static final int COLUMNS = 9;
	public static final int ROWS = 6;
	public static final int SIZE = COLUMNS * ROWS;
	/** Casilla de ayuda, en el centro de la fila inferior. */
	public static final int INFO_SLOT = (ROWS - 1) * COLUMNS + COLUMNS / 2;

	/** Filas que ocupa cada grupo. */
	private static final Map<HordeBoard.Section, List<Integer>> ROWS_OF = Map.of(
			HordeBoard.Section.WARCHIEFS, List.of(0),
			HordeBoard.Section.CAPTAINS, List.of(1, 2),
			HordeBoard.Section.GRUNTS, List.of(3, 4));

	private HordeBoardLayout() {
	}

	/** Contenido de una casilla del menú. */
	public sealed interface Cell permits Empty, Filler, Info, SectionLabel, OrcCell, Overflow {
	}

	public record Empty() implements Cell {
	}

	/** Decoración (cristal oscuro). */
	public record Filler() implements Cell {
	}

	/** Explicación de cómo descubrir más sobre la Horda. */
	public record Info(boolean boardIsEmpty) implements Cell {
	}

	public record SectionLabel(HordeBoard.Section section, int count) implements Cell {
	}

	public record OrcCell(HordeBoard.Entry entry) implements Cell {
	}

	/** "+N más": orcos del grupo que no caben. */
	public record Overflow(HordeBoard.Section section, int hidden) implements Cell {
	}

	public static List<Cell> place(HordeBoard board) {
		Cell[] cells = new Cell[SIZE];
		Arrays.fill(cells, new Empty());
		for (HordeBoard.Section section : HordeBoard.Section.values()) {
			placeSection(cells, section, board.section(section));
		}
		for (int column = 0; column < COLUMNS; column++) {
			cells[(ROWS - 1) * COLUMNS + column] = new Filler();
		}
		cells[INFO_SLOT] = new Info(board.isEmpty());
		return List.of(cells);
	}

	/** Columna 0: etiqueta del grupo; columnas 1 a 8: sus orcos. */
	private static void placeSection(Cell[] cells, HordeBoard.Section section, List<HordeBoard.Entry> entries) {
		List<Integer> rows = ROWS_OF.get(section);
		int capacity = rows.size() * (COLUMNS - 1);
		boolean overflows = entries.size() > capacity;
		int shown = overflows ? capacity - 1 : entries.size();

		for (int i = 0; i < rows.size(); i++) {
			int labelSlot = rows.get(i) * COLUMNS;
			cells[labelSlot] = i == 0 ? new SectionLabel(section, entries.size()) : new Filler();
		}
		for (int i = 0; i < shown; i++) {
			cells[slotFor(rows, i)] = new OrcCell(entries.get(i));
		}
		if (overflows) {
			cells[slotFor(rows, capacity - 1)] = new Overflow(section, entries.size() - shown);
		}
	}

	/** La posición {@code index} dentro de un grupo, saltando la columna de la etiqueta. */
	private static int slotFor(List<Integer> rows, int index) {
		int row = rows.get(index / (COLUMNS - 1));
		int column = 1 + index % (COLUMNS - 1);
		return row * COLUMNS + column;
	}
}
