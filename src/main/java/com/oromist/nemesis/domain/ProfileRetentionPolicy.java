package com.oromist.nemesis.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Decide qué perfiles sobran para que un servidor con muchos jugadores no acumule
 * orcos "recordados" indefinidamente.
 *
 * <ul>
 *   <li>Se conservan como máximo {@code maxDead} perfiles muertos (los más recientes).</li>
 *   <li>Se conservan como máximo {@code maxAliveGrunts} soldados rasos vivos; se descartan
 *       primero los de menos rencor y, a igualdad, los de recuerdos más antiguos.</li>
 *   <li>Los capitanes y caudillos vivos nunca se descartan.</li>
 * </ul>
 */
public record ProfileRetentionPolicy(int maxDead, int maxAliveGrunts) {
	public static final ProfileRetentionPolicy DEFAULT = new ProfileRetentionPolicy(64, 256);

	/** Ids de los perfiles que deben eliminarse. */
	public List<UUID> profilesToRemove(Collection<OrcProfile> profiles) {
		Stream<UUID> dead = excess(profiles.stream().filter(p -> !p.isAlive()).toList(), maxDead,
				Comparator.comparingLong(OrcProfile::lastMemoryTime));
		Stream<UUID> grunts = excess(profiles.stream().filter(p -> p.isAlive() && p.rank() == OrcRank.GRUNT).toList(),
				maxAliveGrunts,
				Comparator.comparingInt(OrcProfile::totalGrudge).thenComparingLong(OrcProfile::lastMemoryTime));
		return Stream.concat(dead, grunts).toList();
	}

	private static Stream<UUID> excess(List<OrcProfile> candidates, int limit, Comparator<OrcProfile> leastValuableFirst) {
		int excess = candidates.size() - limit;
		if (excess <= 0) {
			return Stream.empty();
		}
		return candidates.stream().sorted(leastValuableFirst).limit(excess).map(OrcProfile::id);
	}
}
