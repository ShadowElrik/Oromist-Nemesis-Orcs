package com.oromist.nemesis.domain;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Genera nombres orcos combinando sílabas y un epíteto traducible.
 * Se puede sustituir por otra implementación (ej. nombres desde un archivo de datos).
 */
public interface OrcNameGenerator {
	OrcName generate(RandomGenerator random);

	/** Implementación por defecto basada en sílabas. */
	final class Syllabic implements OrcNameGenerator {
		private static final List<String> PREFIXES = List.of(
				"Gor", "Ug", "Mau", "Ra", "Sha", "Lug", "Bol", "Gri", "Mur", "Az", "Ush", "Grish",
				"Snag", "Ka", "Thra", "Dur", "Ruk", "Zog", "Nar", "Bru", "Skar", "Ghaz", "Orth", "Vrak");
		private static final List<String> SUFFIXES = List.of(
				"bag", "luk", "hur", "dush", "nak", "gash", "rot", "gol", "mog", "grum", "zag",
				"thak", "ruk", "bul", "dak", "nash", "gul", "rag", "mash", "kar");
		/** Claves de traducción de epítetos, ver {@code lang/*.json}. */
		public static final List<String> EPITHETS = List.of(
				"bonecrusher", "skull_taker", "the_cruel", "the_butcher", "flesh_eater", "iron_jaw",
				"the_slow", "black_tongue", "the_proud", "dark_eye", "the_mad", "storm_bringer",
				"of_the_pits", "the_hunter", "the_rabid", "spine_snapper", "the_coward", "head_hunter",
				"the_stinker", "blood_drinker", "the_tracker", "of_the_ashes", "the_loud", "the_drunk");

		@Override
		public OrcName generate(RandomGenerator random) {
			String first = PREFIXES.get(random.nextInt(PREFIXES.size())) + SUFFIXES.get(random.nextInt(SUFFIXES.size()));
			String epithet = EPITHETS.get(random.nextInt(EPITHETS.size()));
			return new OrcName(first, epithetKey(epithet));
		}

		public static String epithetKey(String epithet) {
			return "epithet.oromist-npc." + epithet;
		}
	}
}
