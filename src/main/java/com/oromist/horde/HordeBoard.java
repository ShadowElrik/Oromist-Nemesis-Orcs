package com.oromist.horde;

import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * El tablero de la Horda tal como lo ve un jugador: qué orcos conoce, agrupados por rango,
 * con lo que sabe de cada uno (rasgos descubiertos, rencor, recuerdos).
 *
 * <p>Es Java puro: no sabe nada de menús ni de objetos de Minecraft. La capa {@code game} lo
 * convierte en un menú tipo cofre.</p>
 */
public final class HordeBoard {
	/** Cuántos recuerdos se muestran por orco (los más importantes). */
	public static final int MAX_MEMORIES_SHOWN = 4;

	private final Map<Section, List<Entry>> sections;

	private HordeBoard(Map<Section, List<Entry>> sections) {
		this.sections = sections;
	}

	/**
	 * @param orcs      orcos que el jugador conoce, ya ordenados del más al menos relevante
	 * @param nemesisId el némesis del jugador, si tiene
	 */
	public static HordeBoard of(UUID playerId, List<OrcProfile> orcs, Optional<UUID> nemesisId) {
		Map<Section, List<Entry>> sections = new EnumMap<>(Section.class);
		for (Section section : Section.values()) {
			sections.put(section, new ArrayList<>());
		}
		for (OrcProfile orc : orcs) {
			boolean isNemesis = nemesisId.map(orc.id()::equals).orElse(false);
			sections.get(Section.of(orc.rank())).add(Entry.of(orc, playerId, isNemesis));
		}
		sections.replaceAll((section, entries) -> List.copyOf(entries));
		return new HordeBoard(sections);
	}

	public List<Entry> section(Section section) {
		return sections.get(section);
	}

	public boolean isEmpty() {
		return sections.values().stream().allMatch(List::isEmpty);
	}

	/** Grupos del tablero, de arriba abajo. */
	public enum Section {
		WARCHIEFS,
		CAPTAINS,
		GRUNTS;

		public static Section of(OrcRank rank) {
			return switch (rank) {
				case WARCHIEF -> WARCHIEFS;
				case CAPTAIN -> CAPTAINS;
				case GRUNT -> GRUNTS;
			};
		}
	}

	/**
	 * Un orco del tablero.
	 *
	 * @param knownTraits      rasgos que el jugador ya descubrió
	 * @param hiddenTraitCount rasgos que aún no conoce (se muestran como "???")
	 * @param memories         lo más importante que recuerda del jugador, de mayor a menor importancia
	 */
	public record Entry(OrcProfile profile, boolean nemesis, int grudge, Set<OrcTrait> knownTraits, int hiddenTraitCount,
			List<Memory> memories) {

		static Entry of(OrcProfile orc, UUID playerId, boolean nemesis) {
			List<Memory> memories = orc.memoriesAbout(playerId).stream()
					.filter(memory -> memory.type() != MemoryType.FIRST_ENCOUNTER)
					.sorted(Comparator.comparingInt((Memory m) -> m.type().dialoguePriority())
							.thenComparingLong(Memory::gameTime)
							.reversed())
					.limit(MAX_MEMORIES_SHOWN)
					.toList();
			return new Entry(orc, nemesis, orc.grudgeAgainst(playerId), orc.knownTraits(playerId),
					orc.hiddenTraits(playerId).size(), memories);
		}
	}
}
