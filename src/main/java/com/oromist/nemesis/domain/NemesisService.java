package com.oromist.nemesis.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * Reglas del sistema Némesis: crear orcos notables, registrar lo que recuerdan,
 * ascenderlos y decidir si sobreviven a la muerte.
 *
 * <p>No depende de Minecraft: recibe hechos ya traducidos ({@link EncounterContext})
 * y avisa a los {@link NemesisListener} de lo importante. Esto lo hace testeable y
 * portable a cualquier versión del juego.</p>
 */
public final class NemesisService {
	/** Epíteto que recibe un orco la primera vez que sobrevive a la muerte. */
	public static final String UNDYING_EPITHET = OrcNameGenerator.Syllabic.epithetKey("the_undying");

	private final NemesisRepository repository;
	private final NemesisRules rules;
	private final OrcNameGenerator nameGenerator;
	private final TraitRoller traitRoller;
	private final RandomGenerator random;
	private final List<NemesisListener> listeners = new ArrayList<>();

	public NemesisService(NemesisRepository repository, NemesisRules rules, OrcNameGenerator nameGenerator,
			TraitRoller traitRoller, RandomGenerator random) {
		this.repository = Objects.requireNonNull(repository);
		this.rules = Objects.requireNonNull(rules);
		this.nameGenerator = Objects.requireNonNull(nameGenerator);
		this.traitRoller = Objects.requireNonNull(traitRoller);
		this.random = Objects.requireNonNull(random);
	}

	public void addListener(NemesisListener listener) {
		listeners.add(listener);
	}

	// --- Creación y consulta --------------------------------------------------------

	public OrcProfile createProfile(OrcArchetype archetype, float scale, int appearance, OrcRank startingRank) {
		OrcProfile profile = new OrcProfile(
				UUID.randomUUID(),
				nameGenerator.generate(random),
				archetype,
				scale,
				appearance,
				traitRoller.rollInitial(startingRank, random),
				List.of(),
				startingRank.powerRequired(),
				0,
				0,
				true);
		repository.save(profile);
		return profile;
	}

	public Optional<OrcProfile> find(UUID orcId) {
		return repository.find(orcId);
	}

	/** Orcos vivos que conocen al jugador, del más rencoroso al menos. */
	public List<OrcProfile> enemiesOf(UUID playerId) {
		return repository.all().stream()
				.filter(OrcProfile::isAlive)
				.filter(p -> p.grudgeAgainst(playerId) > 0)
				.sorted(Comparator.comparingInt((OrcProfile p) -> p.grudgeAgainst(playerId)).reversed())
				.toList();
	}

	/** El némesis del jugador: el orco vivo que más lo odia y cumple la condición dada. */
	public Optional<OrcProfile> nemesisOf(UUID playerId, Predicate<OrcProfile> available) {
		return enemiesOf(playerId).stream().filter(available).findFirst();
	}

	// --- Hechos ---------------------------------------------------------------------

	/** El orco ve por primera vez al jugador (no hace nada si ya lo conocía). */
	public boolean recordEncounter(OrcProfile profile, EncounterContext context) {
		if (profile.knows(context.playerId())) {
			return false;
		}
		remember(profile, context, MemoryType.FIRST_ENCOUNTER);
		return true;
	}

	public void onOrcKilledPlayer(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.KILLED_PLAYER);
		grantPower(profile, rules.powerForKillingPlayer());
		listeners.forEach(l -> l.onKilledPlayer(profile, context.playerId()));
	}

	/**
	 * El jugador "mata" al orco. Según su rango puede engañar a la muerte y volver más tarde.
	 */
	public DeathOutcome onOrcSlainByPlayer(OrcProfile profile, EncounterContext context) {
		return onOrcSlainByPlayer(profile, context, false);
	}

	/**
	 * Igual que {@link #onOrcSlainByPlayer(OrcProfile, EncounterContext)}, pero indicando si un
	 * chamán presenció la muerte: sus rituales hacen más probable que el orco vuelva.
	 */
	public DeathOutcome onOrcSlainByPlayer(OrcProfile profile, EncounterContext context, boolean shamanNearby) {
		if (random.nextFloat() < rules.cheatDeathChance(profile.rank(), shamanNearby)) {
			profile.registerCheatedDeath();
			if (profile.timesCheatedDeath() == 1) {
				profile.retitle(UNDYING_EPITHET);
			}
			remember(profile, context, MemoryType.SURVIVED_DEATH);
			grantPower(profile, rules.powerForSurvivingDeath());
			listeners.forEach(l -> l.onCheatedDeath(profile, context.playerId()));
			return DeathOutcome.CHEATED_DEATH;
		}
		profile.markDead();
		repository.markChanged();
		listeners.forEach(l -> l.onDied(profile, context.playerId()));
		return DeathOutcome.DIED;
	}

	public void onOrcFled(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.FLED_FROM_PLAYER);
	}

	public void onPlayerFled(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.PLAYER_FLED);
		grantPower(profile, rules.powerForPlayerFleeing());
	}

	public void onWoundedByPlayer(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.WOUNDED_BY_PLAYER);
	}

	public void onAllyKilledByPlayer(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.PLAYER_KILLED_ALLY);
	}

	public void onMountKilled(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.MOUNT_KILLED);
	}

	/** El jugador lo hizo hablar. Delatar a los suyos no da poder, solo rencor. */
	public void onInterrogated(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.INTERROGATED);
	}

	public void onAmbush(OrcProfile profile, EncounterContext context) {
		remember(profile, context, MemoryType.AMBUSHED_PLAYER);
		grantPower(profile, rules.powerForAmbush());
	}

	/** Registra que el perfil se materializa de nuevo en el mundo; devuelve la nueva encarnación. */
	public int beginIncarnation(OrcProfile profile) {
		int incarnation = profile.nextIncarnation();
		repository.markChanged();
		return incarnation;
	}

	// --- Interno --------------------------------------------------------------------

	private void remember(OrcProfile profile, EncounterContext context, MemoryType type) {
		profile.remember(context.toMemory(type));
		repository.markChanged();
	}

	private void grantPower(OrcProfile profile, int amount) {
		if (amount <= 0) {
			return;
		}
		if (profile.addPower(amount)) {
			traitRoller.rollPromotionTrait(profile, random).ifPresent(profile::gainTrait);
			OrcRank newRank = profile.rank();
			listeners.forEach(l -> l.onPromoted(profile, newRank));
		}
		repository.markChanged();
	}

	public enum DeathOutcome {
		DIED,
		CHEATED_DEATH
	}
}
