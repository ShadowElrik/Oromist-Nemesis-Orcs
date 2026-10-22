package com.oromist.nemesis.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Información de guerra: qué sabe cada jugador de los rasgos de los orcos.
 *
 * <p>Los rasgos de un orco empiezan ocultos para todos. Un jugador los descubre viéndolos en
 * combate ({@link #reveal}), interrogando a un orco malherido o leyendo unas notas de guerra
 * ({@link #extractIntel}). Saber que tu némesis "teme al fuego" convierte una pelea imposible
 * en una oportunidad.</p>
 */
public final class IntelService {
	/** Entre cuántos orcos "interesantes" se elige al azar de quién se habla. */
	private static final int SHORTLIST = 3;

	private final NemesisRepository repository;
	private final RandomGenerator random;

	public IntelService(NemesisRepository repository, RandomGenerator random) {
		this.repository = Objects.requireNonNull(repository);
		this.random = Objects.requireNonNull(random);
	}

	/** Un secreto descubierto: el rasgo {@code trait} del orco {@code subject}. */
	public record Intel(OrcProfile subject, OrcTrait trait) {
	}

	/**
	 * El jugador ve un rasgo en acción (p. ej. sus flechas apenas hieren al orco).
	 *
	 * @return {@code true} si no lo sabía
	 */
	public boolean reveal(OrcProfile profile, UUID playerId, OrcTrait trait) {
		boolean isNew = profile.revealTraitTo(playerId, trait);
		if (isNew) {
			repository.markChanged();
		}
		return isNew;
	}

	/** Secreto obtenido de unas notas de guerra. */
	public Optional<Intel> extractIntel(UUID playerId) {
		return extractIntel(playerId, Optional.empty());
	}

	/**
	 * Un informante revela un secreto útil sobre otro orco: primero de los que odian al jugador,
	 * luego de los capitanes y caudillos. Prefiere debilidades, que es lo que más sirve.
	 *
	 * @param informant el orco que habla (nunca se delata a sí mismo)
	 * @return vacío si no queda nada que contar
	 */
	public Optional<Intel> extractIntel(UUID playerId, Optional<UUID> informant) {
		List<OrcProfile> shortlist = repository.all().stream()
				.filter(OrcProfile::isAlive)
				.filter(profile -> informant.map(id -> !id.equals(profile.id())).orElse(true))
				.filter(profile -> profile.grudgeAgainst(playerId) > 0 || profile.rank().isNamed())
				.filter(profile -> !profile.hiddenTraits(playerId).isEmpty())
				.sorted(byRelevanceTo(playerId))
				.limit(SHORTLIST)
				.toList();
		if (shortlist.isEmpty()) {
			return Optional.empty();
		}
		OrcProfile subject = shortlist.get(random.nextInt(shortlist.size()));
		OrcTrait trait = pickSecret(subject.hiddenTraits(playerId));
		reveal(subject, playerId, trait);
		return Optional.of(new Intel(subject, trait));
	}

	/** Los que más odian al jugador primero; a igual rencor, los de mayor rango y poder. */
	private static Comparator<OrcProfile> byRelevanceTo(UUID playerId) {
		return Comparator.comparingInt((OrcProfile profile) -> profile.grudgeAgainst(playerId))
				.thenComparing(OrcProfile::rank)
				.thenComparingInt(OrcProfile::power)
				.reversed();
	}

	private OrcTrait pickSecret(Set<OrcTrait> hidden) {
		List<OrcTrait> weaknesses = hidden.stream().filter(t -> t.kind() == OrcTrait.TraitKind.WEAKNESS).toList();
		List<OrcTrait> pool = weaknesses.isEmpty() ? List.copyOf(hidden) : weaknesses;
		return pool.get(random.nextInt(pool.size()));
	}
}
