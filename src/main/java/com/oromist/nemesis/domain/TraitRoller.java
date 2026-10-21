package com.oromist.nemesis.domain;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Decide qué rasgos recibe un orco. Separado del perfil para poder cambiar la
 * "receta" de rasgos sin tocar el resto del sistema.
 */
public interface TraitRoller {
	Set<OrcTrait> rollInitial(OrcRank rank, RandomGenerator random);

	/** Rasgo extra al ascender, si hay alguno disponible. */
	Optional<OrcTrait> rollPromotionTrait(OrcProfile profile, RandomGenerator random);

	/** Implementación por defecto: más rangos, más rasgos; como mucho una debilidad inicial. */
	final class Default implements TraitRoller {
		@Override
		public Set<OrcTrait> rollInitial(OrcRank rank, RandomGenerator random) {
			int strengths = switch (rank) {
				case GRUNT -> random.nextFloat() < 0.3f ? 1 : 0;
				case CAPTAIN -> 1 + random.nextInt(2);
				case WARCHIEF -> 2 + random.nextInt(2);
			};
			Set<OrcTrait> traits = EnumSet.noneOf(OrcTrait.class);
			pickRandom(OrcTrait.TraitKind.STRENGTH, strengths, traits, random);
			if (random.nextFloat() < 0.6f) {
				pickRandom(OrcTrait.TraitKind.WEAKNESS, 1, traits, random);
			}
			return traits;
		}

		@Override
		public Optional<OrcTrait> rollPromotionTrait(OrcProfile profile, RandomGenerator random) {
			List<OrcTrait> candidates = candidates(OrcTrait.TraitKind.STRENGTH, profile.traits());
			return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(random.nextInt(candidates.size())));
		}

		private static void pickRandom(OrcTrait.TraitKind kind, int amount, Set<OrcTrait> into, RandomGenerator random) {
			for (int i = 0; i < amount; i++) {
				List<OrcTrait> candidates = candidates(kind, into);
				if (candidates.isEmpty()) {
					return;
				}
				into.add(candidates.get(random.nextInt(candidates.size())));
			}
		}

		private static List<OrcTrait> candidates(OrcTrait.TraitKind kind, Set<OrcTrait> excluded) {
			List<OrcTrait> result = new ArrayList<>();
			for (OrcTrait trait : OrcTrait.values()) {
				if (trait.kind() == kind && !excluded.contains(trait)) {
					result.add(trait);
				}
			}
			return result;
		}
	}
}
