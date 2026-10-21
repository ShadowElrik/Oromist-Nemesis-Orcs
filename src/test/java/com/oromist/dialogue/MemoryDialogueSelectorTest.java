package com.oromist.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oromist.nemesis.domain.EncounterContext;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcName;
import com.oromist.nemesis.domain.OrcProfile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemoryDialogueSelectorTest {
	private static final UUID STEVE = UUID.randomUUID();
	private static final EncounterContext CONTEXT = new EncounterContext(STEVE, "Steve", 2 * 24000L, "minecraft:taiga");

	private final MemoryDialogueSelector selector = new MemoryDialogueSelector();

	private static OrcProfile orcRemembering(MemoryType type, int times) {
		OrcProfile profile = new OrcProfile(UUID.randomUUID(), new OrcName("Gorbag", ""), OrcArchetype.AXEMAN, 1.0f, 0,
				Set.of(), List.of(), 3, 0, 0, true);
		for (int i = 0; i < times; i++) {
			profile.remember(CONTEXT.toMemory(type));
		}
		return profile;
	}

	/** Todas las frases que el código puede pedir deben existir en todos los idiomas. */
	@Test
	void everyReachableDialogueKeyIsTranslated() throws IOException {
		Set<String> keys = new TreeSet<>();
		for (MemoryType type : MemoryType.values()) {
			for (int times = 1; times <= 2; times++) {
				OrcProfile orc = orcRemembering(type, times);
				for (DialogueTrigger trigger : DialogueTrigger.values()) {
					for (int seed = 0; seed < 50; seed++) {
						keys.add(selector.select(orc, STEVE, "Steve", trigger, 10 * 24000L, new SplittableRandom(seed))
								.orElseThrow().translationKey());
					}
				}
			}
		}
		for (String language : List.of("es_es", "es_cl", "en_us")) {
			String json = readLang(language);
			for (String key : keys) {
				assertTrue(json.contains("\"" + key + "\""), "falta " + key + " en " + language);
			}
		}
	}

	@Test
	void sightingUsesTheMostImportantMemoryAndItsContext() {
		OrcProfile orc = orcRemembering(MemoryType.SURVIVED_DEATH, 1);

		DialogueLine line = selector.select(orc, STEVE, "Steve", DialogueTrigger.SIGHTED, 5 * 24000L, new SplittableRandom(1))
				.orElseThrow();

		assertTrue(line.translationKey().startsWith("dialogue.oromist-npc.survived_death."));
		assertEquals(new DialogueLine.Literal("Steve"), line.args().get(0));
		assertEquals(new DialogueLine.Number(3), line.args().get(1), "días desde el recuerdo");
		assertEquals(new DialogueLine.Translatable("biome.minecraft.taiga"), line.args().get(2));
	}

	@Test
	void strangersGetAFirstMeetingLine() {
		OrcProfile orc = orcRemembering(MemoryType.FIRST_ENCOUNTER, 1);

		DialogueLine line = selector.select(orc, STEVE, "Steve", DialogueTrigger.SIGHTED, 0, new SplittableRandom(1))
				.orElseThrow();

		assertTrue(line.translationKey().startsWith("dialogue.oromist-npc.first_meeting."));
	}

	private static String readLang(String language) throws IOException {
		try (InputStream in = MemoryDialogueSelectorTest.class.getResourceAsStream("/assets/oromist-npc/lang/" + language + ".json")) {
			assertNotNull(in, "no se encontró el idioma " + language);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
