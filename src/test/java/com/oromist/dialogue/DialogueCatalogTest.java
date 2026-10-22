package com.oromist.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oromist.nemesis.domain.OrcNameGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class DialogueCatalogTest {
	@Test
	void countsTheVariantsOfEachCategory() {
		DialogueCatalog catalog = DialogueCatalog.parse("""
				{
				  "dialogue.oromist-npc.dying.0": "a",
				  "dialogue.oromist-npc.dying.1": "b",
				  "dialogue.oromist-npc.dying.2": "c",
				  "dialogue.oromist-npc.fleeing.0": "d",
				  "entity.oromist-npc.orc": "Orc"
				}
				""");

		assertEquals(3, catalog.variants("dying"));
		assertEquals(1, catalog.variants("fleeing"));
		assertEquals(1, catalog.variants("unknown_category"), "una categoría desconocida no debe fallar");
	}

	/** Todos los idiomas deben tener exactamente las mismas frases que el idioma de referencia. */
	@Test
	void everyLanguageHasTheSameDialogueLines() throws IOException {
		DialogueCatalog reference = DialogueCatalog.fromModResources();
		assertTrue(reference.variants("first_meeting") > 1, "el catálogo de referencia se cargó");
		for (String language : List.of("es_es", "es_cl")) {
			assertEquals(reference.asMap(), DialogueCatalog.parse(readLang(language)).asMap(), "frases distintas en " + language);
		}
	}

	@Test
	void everyEpithetIsTranslated() throws IOException {
		for (String language : List.of("es_es", "es_cl", "en_us")) {
			String json = readLang(language);
			for (String epithet : OrcNameGenerator.Syllabic.EPITHETS) {
				String key = OrcNameGenerator.Syllabic.epithetKey(epithet);
				assertTrue(json.contains("\"" + key + "\""), "falta " + key + " en " + language);
			}
		}
	}

	/** Cada recuerdo y cada situación que puede hacer hablar a un orco tiene al menos una frase. */
	@Test
	void everyCategoryTheSelectorUsesHasLines() throws IOException {
		for (String language : List.of("es_es", "es_cl", "en_us")) {
			String json = readLang(language);
			for (String category : MemoryDialogueSelector.allCategories()) {
				assertTrue(json.contains("\"dialogue.oromist-npc." + category + ".0\""),
						"faltan frases de '" + category + "' en " + language);
			}
		}
	}

	/** Los tres idiomas tienen exactamente las mismas claves (frases, menús, objetos...). */
	@Test
	void everyLanguageHasTheSameKeys() throws IOException {
		java.util.Set<String> reference = keys(readLang("en_us"));
		for (String language : List.of("es_es", "es_cl")) {
			assertEquals(reference, keys(readLang(language)), "claves distintas en " + language);
		}
	}

	/** Cada tipo de recuerdo tiene su texto en el tablero de la Horda. */
	@Test
	void everyMemoryTypeCanBeDescribed() throws IOException {
		java.util.Set<String> keys = keys(readLang("en_us"));
		for (com.oromist.nemesis.domain.MemoryType type : com.oromist.nemesis.domain.MemoryType.values()) {
			String key = "memory.oromist-npc." + type.name().toLowerCase(java.util.Locale.ROOT);
			assertTrue(keys.contains(key), "falta " + key);
		}
	}

	private static java.util.Set<String> keys(String json) {
		return com.google.gson.JsonParser.parseString(json).getAsJsonObject().keySet();
	}

	private static String readLang(String language) throws IOException {
		try (InputStream in = DialogueCatalogTest.class.getResourceAsStream("/assets/oromist-npc/lang/" + language + ".json")) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
