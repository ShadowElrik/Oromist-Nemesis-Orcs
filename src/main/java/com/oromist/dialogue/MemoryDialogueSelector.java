package com.oromist.dialogue;

import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Selector de diálogos basado en recuerdos.
 *
 * <p>Las claves siguen el patrón {@code dialogue.oromist-npc.<categoría>.<n>}; para añadir
 * frases basta con agregarlas a los archivos de idioma con el siguiente número libre
 * (ver {@link DialogueCatalog}). Argumentos disponibles en todas las frases:</p>
 * <ol>
 *   <li>{@code %1$s} nombre del jugador</li>
 *   <li>{@code %2$s} días transcurridos desde el recuerdo</li>
 *   <li>{@code %3$s} lugar (bioma) del recuerdo</li>
 *   <li>{@code %4$s} veces que ocurrió ese hecho</li>
 * </ol>
 */
public final class MemoryDialogueSelector implements DialogueSelector {
	private static final String PREFIX = "dialogue.oromist-npc.";
	private static final String FIRST_MEETING = "first_meeting";

	private final DialogueCatalog catalog;

	/** Usa las frases incluidas en el mod. */
	public MemoryDialogueSelector() {
		this(DialogueCatalog.fromModResources());
	}

	public MemoryDialogueSelector(DialogueCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public Optional<DialogueLine> select(OrcProfile speaker, UUID playerId, String playerName, DialogueTrigger trigger,
			long gameTime, RandomGenerator random) {
		Optional<Memory> memory = speaker.mostSignificantMemory(playerId);
		String category = trigger == DialogueTrigger.SIGHTED
				? memory.map(m -> categoryFor(speaker, playerId, m)).orElse(FIRST_MEETING)
				: eventCategory(trigger);
		return Optional.of(buildLine(category, speaker, playerId, playerName, memory, gameTime, random));
	}

	/** Categoría de los disparadores que no dependen de recuerdos. */
	private static String eventCategory(DialogueTrigger trigger) {
		return switch (trigger) {
			case SIGHTED -> FIRST_MEETING;
			case KILLED_PLAYER -> "taunt_kill";
			case FLEEING -> "fleeing";
			case PROMOTED -> "promoted";
			case AMBUSH_HOME -> "ambush_home";
			case AMBUSH_MINING -> "ambush_mining";
			case AMBUSH_TRAVEL -> "ambush_travel";
			case DYING -> "dying";
			case MOUNT_LOST -> "mount_lost";
			case INTERROGATION_TALK -> "interrogation_talk";
			case INTERROGATION_REFUSE -> "interrogation_refuse";
		};
	}

	/** Recuerdos que tienen frases distintas la primera vez y las siguientes ("_again"). */
	private static boolean hasRepeatVariant(MemoryType type) {
		return type == MemoryType.KILLED_PLAYER || type == MemoryType.SURVIVED_DEATH;
	}

	private static String categoryFor(OrcProfile speaker, UUID playerId, Memory memory) {
		long times = speaker.countMemories(playerId, memory.type());
		String base = memoryCategory(memory.type());
		return hasRepeatVariant(memory.type()) && times > 1 ? base + "_again" : base;
	}

	private static String memoryCategory(MemoryType type) {
		return type.name().toLowerCase(Locale.ROOT);
	}

	/**
	 * Todas las categorías que este selector puede pedir. Las pruebas comprueban que cada una
	 * tenga frases en todos los idiomas, así que añadir un recuerdo o disparador sin frases falla pronto.
	 */
	public static Set<String> allCategories() {
		Set<String> categories = new TreeSet<>();
		for (DialogueTrigger trigger : DialogueTrigger.values()) {
			categories.add(eventCategory(trigger));
		}
		for (MemoryType type : MemoryType.values()) {
			if (type == MemoryType.FIRST_ENCOUNTER) {
				continue; // Nunca se usa para hablar: se saluda con "first_meeting".
			}
			categories.add(memoryCategory(type));
			if (hasRepeatVariant(type)) {
				categories.add(memoryCategory(type) + "_again");
			}
		}
		return categories;
	}

	private DialogueLine buildLine(String category, OrcProfile speaker, UUID playerId, String playerName,
			Optional<Memory> memory, long gameTime, RandomGenerator random) {
		int variants = catalog.variants(category);
		String key = PREFIX + category + "." + random.nextInt(variants);

		long daysAgo = memory.map(m -> m.daysAgo(gameTime)).orElse(0L);
		String place = memory.map(Memory::place).orElse("");
		long times = memory.map(m -> speaker.countMemories(playerId, m.type())).orElse(0L);

		List<DialogueLine.Arg> args = new ArrayList<>();
		args.add(new DialogueLine.Literal(playerName));
		args.add(new DialogueLine.Number(daysAgo));
		args.add(new DialogueLine.Translatable(placeTranslationKey(place)));
		args.add(new DialogueLine.Number(times));
		return new DialogueLine(key, args);
	}

	/**
	 * Clave de traducción del lugar de un recuerdo: "minecraft:dark_forest" → "biome.minecraft.dark_forest"
	 * (la clave vanilla del bioma), o "lugar desconocido" si no se guardó.
	 */
	public static String placeTranslationKey(String place) {
		return place.isEmpty() ? "place.oromist-npc.unknown" : "biome." + place.replace(':', '.');
	}
}
