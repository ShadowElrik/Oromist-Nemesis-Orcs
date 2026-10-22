package com.oromist.nemesis.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Identidad persistente de un orco notable: sobrevive a que su entidad se
 * descargue o "muera", y es lo que permite que vuelva a aparecer recordando al jugador.
 *
 * <p>La entidad en el mundo solo guarda el {@link #id()} y la {@link #incarnation()}
 * con la que fue creada; todo lo demás vive aquí.</p>
 */
public final class OrcProfile {
	/** Máximo de recuerdos guardados; se descartan primero los menos importantes. */
	public static final int MAX_MEMORIES = 24;
	/** Apariencia no registrada (perfiles creados por la versión 0.1.0). */
	public static final int UNKNOWN_APPEARANCE = -1;

	private final UUID id;
	private OrcName name;
	private final OrcArchetype archetype;
	private final float scale;
	private final int appearance;
	private final Set<OrcTrait> traits;
	private final List<Memory> memories;
	private int power;
	private int timesCheatedDeath;
	private int incarnation;
	private boolean alive;
	/** Rasgos que cada jugador ya descubrió de este orco (el resto se muestran como "???"). */
	private final Map<UUID, Set<OrcTrait>> revealedTraits = new HashMap<>();

	public OrcProfile(UUID id, OrcName name, OrcArchetype archetype, float scale, int appearance, Set<OrcTrait> traits,
			List<Memory> memories, int power, int timesCheatedDeath, int incarnation, boolean alive) {
		this(id, name, archetype, scale, appearance, traits, memories, power, timesCheatedDeath, incarnation, alive, Map.of());
	}

	public OrcProfile(UUID id, OrcName name, OrcArchetype archetype, float scale, int appearance, Set<OrcTrait> traits,
			List<Memory> memories, int power, int timesCheatedDeath, int incarnation, boolean alive,
			Map<UUID, ? extends Set<OrcTrait>> knownByPlayer) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.archetype = Objects.requireNonNull(archetype, "archetype");
		this.scale = scale;
		this.appearance = appearance;
		this.traits = traits.isEmpty() ? EnumSet.noneOf(OrcTrait.class) : EnumSet.copyOf(traits);
		this.memories = new ArrayList<>(memories);
		this.power = power;
		this.timesCheatedDeath = timesCheatedDeath;
		this.incarnation = incarnation;
		this.alive = alive;
		knownByPlayer.forEach((player, known) -> known.forEach(trait -> revealTraitTo(player, trait)));
	}

	// --- Identidad -------------------------------------------------------------

	public UUID id() {
		return id;
	}

	public OrcName name() {
		return name;
	}

	/** Cambia el epíteto (ej. tras sobrevivir a la muerte pasa a ser "el Inmortal"). */
	public void retitle(String epithetKey) {
		this.name = new OrcName(name.firstName(), epithetKey);
	}

	public OrcArchetype archetype() {
		return archetype;
	}

	/** Multiplicador de tamaño (1.0 = altura de un jugador). */
	public float scale() {
		return scale;
	}

	/**
	 * Variante visual (piel) del orco, para que se vea igual cada vez que reaparece.
	 * La capa de presentación decide cómo interpretarla.
	 */
	public int appearance() {
		// Compatibilidad: los perfiles antiguos usaban el hash del id para elegir la piel.
		return appearance == UNKNOWN_APPEARANCE ? id.hashCode() : appearance;
	}

	public Set<OrcTrait> traits() {
		return Collections.unmodifiableSet(traits);
	}

	public boolean hasTrait(OrcTrait trait) {
		return traits.contains(trait);
	}

	/** @return {@code true} si el rasgo es nuevo */
	public boolean gainTrait(OrcTrait trait) {
		return traits.add(trait);
	}

	// --- Información descubierta por los jugadores ----------------------------------

	/**
	 * El jugador descubre uno de los rasgos de este orco.
	 *
	 * @return {@code true} si es un descubrimiento nuevo (el orco tiene el rasgo y el jugador no lo sabía)
	 */
	public boolean revealTraitTo(UUID playerId, OrcTrait trait) {
		if (!traits.contains(trait)) {
			return false;
		}
		return revealedTraits.computeIfAbsent(playerId, id -> EnumSet.noneOf(OrcTrait.class)).add(trait);
	}

	/** Rasgos de este orco que el jugador conoce. */
	public Set<OrcTrait> knownTraits(UUID playerId) {
		Set<OrcTrait> known = EnumSet.noneOf(OrcTrait.class);
		known.addAll(traits);
		known.retainAll(revealedTraits.getOrDefault(playerId, Set.of()));
		return Collections.unmodifiableSet(known);
	}

	/** Rasgos de este orco que el jugador todavía no conoce. */
	public Set<OrcTrait> hiddenTraits(UUID playerId) {
		Set<OrcTrait> hidden = traits.isEmpty() ? EnumSet.noneOf(OrcTrait.class) : EnumSet.copyOf(traits);
		hidden.removeAll(revealedTraits.getOrDefault(playerId, Set.of()));
		return Collections.unmodifiableSet(hidden);
	}

	/** Todo lo descubierto, por jugador (para guardarlo). */
	public Map<UUID, Set<OrcTrait>> revealedTraits() {
		Map<UUID, Set<OrcTrait>> copy = new HashMap<>();
		revealedTraits.forEach((player, known) -> copy.put(player, Collections.unmodifiableSet(EnumSet.copyOf(known))));
		return Collections.unmodifiableMap(copy);
	}

	// --- Progresión --------------------------------------------------------------

	public int power() {
		return power;
	}

	public OrcRank rank() {
		return OrcRank.forPower(power);
	}

	/**
	 * Suma poder al orco.
	 *
	 * @return {@code true} si con ello subió de rango
	 */
	public boolean addPower(int amount) {
		OrcRank before = rank();
		power = Math.max(0, power + amount);
		return rank().ordinal() > before.ordinal();
	}

	// --- Vida y muerte -------------------------------------------------------------

	public boolean isAlive() {
		return alive;
	}

	public void markDead() {
		alive = false;
	}

	public int timesCheatedDeath() {
		return timesCheatedDeath;
	}

	public void registerCheatedDeath() {
		timesCheatedDeath++;
	}

	/**
	 * Cada vez que el orco se materializa en el mundo se incrementa este contador.
	 * Una entidad con una encarnación antigua es un "eco" y debe desaparecer,
	 * evitando duplicados del mismo orco.
	 */
	public int incarnation() {
		return incarnation;
	}

	public int nextIncarnation() {
		return ++incarnation;
	}

	// --- Memoria ------------------------------------------------------------------

	public List<Memory> memories() {
		return Collections.unmodifiableList(memories);
	}

	public void remember(Memory memory) {
		memories.add(memory);
		while (memories.size() > MAX_MEMORIES) {
			memories.stream()
					.min(Comparator.comparingInt((Memory m) -> m.type().dialoguePriority()).thenComparingLong(Memory::gameTime))
					.ifPresent(memories::remove);
		}
	}

	public List<Memory> memoriesAbout(UUID playerId) {
		return memories.stream().filter(m -> m.isAbout(playerId)).toList();
	}

	public boolean knows(UUID playerId) {
		return memories.stream().anyMatch(m -> m.isAbout(playerId));
	}

	public long countMemories(UUID playerId, MemoryType type) {
		return memories.stream().filter(m -> m.isAbout(playerId) && m.type() == type).count();
	}

	/** Recuerdo más relevante para hablar con ese jugador (mayor prioridad, y luego el más reciente). */
	public Optional<Memory> mostSignificantMemory(UUID playerId) {
		return memories.stream()
				.filter(m -> m.isAbout(playerId) && m.type() != MemoryType.FIRST_ENCOUNTER)
				.max(Comparator.comparingInt((Memory m) -> m.type().dialoguePriority()).thenComparingLong(Memory::gameTime));
	}

	/** Momento del recuerdo más reciente, o {@code Long.MIN_VALUE} si no recuerda nada. */
	public long lastMemoryTime() {
		return memories.stream().mapToLong(Memory::gameTime).max().orElse(Long.MIN_VALUE);
	}

	/** Rencor total contra todos los jugadores: mide lo "importante" que es este orco. */
	public int totalGrudge() {
		return memories.stream().mapToInt(m -> m.type().grudgeWeight()).sum();
	}

	/** Cuánto odia este orco al jugador: base para elegir al némesis. */
	public int grudgeAgainst(UUID playerId) {
		return memories.stream().filter(m -> m.isAbout(playerId)).mapToInt(m -> m.type().grudgeWeight()).sum();
	}
}
