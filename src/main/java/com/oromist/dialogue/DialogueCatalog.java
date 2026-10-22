package com.oromist.dialogue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cuántas frases hay en cada categoría de diálogo.
 *
 * <p>Se calcula leyendo el archivo de idioma de referencia ({@code en_us.json}), así que
 * para añadir frases basta con escribirlas en los archivos de idioma con el siguiente
 * número libre ({@code dialogue.oromist-npc.<categoría>.<n>}): no hay que tocar código.</p>
 */
public final class DialogueCatalog {
	/** Idioma que define qué frases existen; los demás idiomas deben tener las mismas claves. */
	public static final String REFERENCE_LANGUAGE = "/assets/oromist-npc/lang/en_us.json";

	private static final Pattern KEY = Pattern.compile("\"dialogue\\.oromist-npc\\.([a-z_]+)\\.(\\d+)\"\\s*:");

	private final Map<String, Integer> variants;

	private DialogueCatalog(Map<String, Integer> variants) {
		this.variants = Map.copyOf(variants);
	}

	/** Cuenta las frases de cada categoría en el texto de un archivo de idioma. */
	public static DialogueCatalog parse(String languageJson) {
		Map<String, Integer> counts = new HashMap<>();
		Matcher matcher = KEY.matcher(languageJson);
		while (matcher.find()) {
			int index = Integer.parseInt(matcher.group(2));
			counts.merge(matcher.group(1), index + 1, Math::max);
		}
		return new DialogueCatalog(counts);
	}

	/** Lee el idioma de referencia incluido en el propio mod (funciona igual en cliente y servidor). */
	public static DialogueCatalog fromModResources() {
		try (InputStream in = DialogueCatalog.class.getResourceAsStream(REFERENCE_LANGUAGE)) {
			if (in == null) {
				throw new IllegalStateException("No se encontró " + REFERENCE_LANGUAGE + " dentro del mod");
			}
			return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Número de frases de la categoría (al menos 1, para no fallar si falta alguna). */
	public int variants(String category) {
		return Math.max(1, variants.getOrDefault(category, 1));
	}

	public Map<String, Integer> asMap() {
		return variants;
	}
}
