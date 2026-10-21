package com.oromist.dialogue;

import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Selector de diálogos basado en recuerdos.
 *
 * <p>Las claves siguen el patrón {@code dialogue.oromist-npc.<categoría>.<n>}; para añadir
 * frases basta con agregarlas a los archivos de idioma y subir el número de variantes
 * en {@link #VARIANTS}. Argumentos disponibles en todas las frases:</p>
 * <ol>
 *   <li>{@code %1$s} nombre del jugador</li>
 *   <li>{@code %2$s} días transcurridos desde el recuerdo</li>
 *   <li>{@code %3$s} lugar (bioma) del recuerdo</li>
 *   <li>{@code %4$s} veces que ocurrió ese hecho</li>
 * </ol>
 */
public final class MemoryDialogueSelector implements DialogueSelector {
	private static final String PREFIX = "dialogue.oromist-npc.";
	private static final long TICKS_PER_DAY = 24000L;

	/** Cuántas variantes existen por categoría en los archivos de idioma. */
	private static final Map<String, Integer> VARIANTS = Map.ofEntries(
			Map.entry("first_meeting", 5),
			Map.entry("wounded_by_player", 3),
			Map.entry("fled_from_player", 3),
			Map.entry("player_fled", 3),
			Map.entry("player_killed_ally", 3),
			Map.entry("ambushed_player", 2),
			Map.entry("killed_player", 4),
			Map.entry("killed_player_again", 3),
			Map.entry("survived_death", 4),
			Map.entry("survived_death_again", 2),
			Map.entry("taunt_kill", 4),
			Map.entry("fleeing", 3),
			Map.entry("promoted", 3),
			Map.entry("ambush_home", 3),
			Map.entry("ambush_mining", 3),
			Map.entry("ambush_travel", 3),
			Map.entry("dying", 4));

	@Override
	public Optional<DialogueLine> select(OrcProfile speaker, UUID playerId, String playerName, DialogueTrigger trigger,
			long gameTime, RandomGenerator random) {
		Optional<Memory> memory = speaker.mostSignificantMemory(playerId);
		String category = switch (trigger) {
			case SIGHTED -> memory.map(m -> categoryFor(speaker, playerId, m)).orElse("first_meeting");
			case KILLED_PLAYER -> "taunt_kill";
			case FLEEING -> "fleeing";
			case PROMOTED -> "promoted";
			case AMBUSH_HOME -> "ambush_home";
			case AMBUSH_MINING -> "ambush_mining";
			case AMBUSH_TRAVEL -> "ambush_travel";
			case DYING -> "dying";
		};
		return Optional.of(buildLine(category, speaker, playerId, playerName, memory, gameTime, random));
	}

	private static String categoryFor(OrcProfile speaker, UUID playerId, Memory memory) {
		long times = speaker.countMemories(playerId, memory.type());
		String base = memory.type().name().toLowerCase(Locale.ROOT);
		boolean repeatable = memory.type() == MemoryType.KILLED_PLAYER || memory.type() == MemoryType.SURVIVED_DEATH;
		return repeatable && times > 1 ? base + "_again" : base;
	}

	private static DialogueLine buildLine(String category, OrcProfile speaker, UUID playerId, String playerName,
			Optional<Memory> memory, long gameTime, RandomGenerator random) {
		int variants = VARIANTS.getOrDefault(category, 1);
		String key = PREFIX + category + "." + random.nextInt(variants);

		long daysAgo = memory.map(m -> Math.max(0, (gameTime - m.gameTime()) / TICKS_PER_DAY)).orElse(0L);
		String place = memory.map(Memory::place).orElse("");
		long times = memory.map(m -> speaker.countMemories(playerId, m.type())).orElse(0L);

		List<DialogueLine.Arg> args = new ArrayList<>();
		args.add(new DialogueLine.Literal(playerName));
		args.add(new DialogueLine.Number(daysAgo));
		args.add(place.isEmpty() ? new DialogueLine.Translatable("place.oromist-npc.unknown") : new DialogueLine.Translatable(biomeKey(place)));
		args.add(new DialogueLine.Number(times));
		return new DialogueLine(key, args);
	}

	/** "minecraft:dark_forest" → "biome.minecraft.dark_forest" (clave vanilla del bioma). */
	private static String biomeKey(String place) {
		return "biome." + place.replace(':', '.');
	}
}
