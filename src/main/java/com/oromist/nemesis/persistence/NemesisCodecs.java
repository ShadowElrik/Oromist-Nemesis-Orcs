package com.oromist.nemesis.persistence;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcName;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codecs de serialización del dominio. Están separados del dominio para que este
 * no dependa de la librería de serialización del juego.
 */
public final class NemesisCodecs {
	private NemesisCodecs() {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("oromist-npc/persistence");

	/**
	 * Codec de enums por nombre. Un valor desconocido (por ejemplo, un rasgo eliminado en
	 * una versión futura) produce un error de lectura controlado, nunca una excepción.
	 */
	public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> type) {
		return Codec.STRING.comapFlatMap(
				name -> {
					try {
						return DataResult.success(Enum.valueOf(type, name.toUpperCase(Locale.ROOT)));
					} catch (IllegalArgumentException e) {
						return DataResult.error(() -> "Valor desconocido para " + type.getSimpleName() + ": " + name);
					}
				},
				value -> value.name().toLowerCase(Locale.ROOT));
	}

	/**
	 * Lista que, al leer, descarta (y registra) los elementos inválidos en lugar de fallar
	 * entera. Así un solo dato corrupto no borra la memoria de todos los orcos.
	 */
	public static <A> Codec<List<A>> lenientList(Codec<A> element) {
		Decoder<List<A>> lenient = new Decoder<>() {
			@Override
			public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
				return ops.getStream(input).map(stream -> {
					List<A> result = new ArrayList<>();
					stream.forEach(entry -> element.parse(ops, entry)
							.resultOrPartial(error -> LOGGER.warn("Se descarta un dato Némesis inválido: {}", error))
							.ifPresent(result::add));
					return Pair.of(result, input);
				});
			}
		};
		return Codec.of(element.listOf(), lenient);
	}

	public static final Codec<MemoryType> MEMORY_TYPE = enumCodec(MemoryType.class);
	public static final Codec<OrcTrait> TRAIT = enumCodec(OrcTrait.class);
	public static final Codec<OrcArchetype> ARCHETYPE = enumCodec(OrcArchetype.class);

	public static final Codec<Memory> MEMORY = RecordCodecBuilder.create(i -> i.group(
			MEMORY_TYPE.fieldOf("type").forGetter(Memory::type),
			UUIDUtil.CODEC.fieldOf("player").forGetter(Memory::playerId),
			Codec.STRING.optionalFieldOf("player_name", "").forGetter(Memory::playerName),
			Codec.LONG.fieldOf("time").forGetter(Memory::gameTime),
			Codec.STRING.optionalFieldOf("place", "").forGetter(Memory::place)
	).apply(i, Memory::new));

	public static final Codec<OrcName> NAME = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("first").forGetter(OrcName::firstName),
			Codec.STRING.optionalFieldOf("epithet", "").forGetter(OrcName::epithetKey)
	).apply(i, OrcName::new));

	/** Lo que un jugador sabe de un orco: {@code {player: ..., traits: [...]}}. */
	private record PlayerIntel(UUID player, List<OrcTrait> traits) {
	}

	private static final Codec<PlayerIntel> PLAYER_INTEL = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.CODEC.fieldOf("player").forGetter(PlayerIntel::player),
			lenientList(TRAIT).fieldOf("traits").forGetter(PlayerIntel::traits)
	).apply(i, PlayerIntel::new));

	private static List<PlayerIntel> intelOf(OrcProfile profile) {
		return profile.revealedTraits().entrySet().stream()
				.map(entry -> new PlayerIntel(entry.getKey(), List.copyOf(entry.getValue())))
				.toList();
	}

	public static final Codec<OrcProfile> PROFILE = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(OrcProfile::id),
			NAME.fieldOf("name").forGetter(OrcProfile::name),
			ARCHETYPE.fieldOf("archetype").forGetter(OrcProfile::archetype),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(OrcProfile::scale),
			// -1 = guardado con la 0.1.0 (sin este campo): se deriva del id como entonces.
			Codec.INT.optionalFieldOf("appearance", OrcProfile.UNKNOWN_APPEARANCE).forGetter(OrcProfile::appearance),
			lenientList(TRAIT).optionalFieldOf("traits", List.of()).forGetter(p -> List.copyOf(p.traits())),
			lenientList(MEMORY).optionalFieldOf("memories", List.of()).forGetter(OrcProfile::memories),
			Codec.INT.optionalFieldOf("power", 0).forGetter(OrcProfile::power),
			Codec.INT.optionalFieldOf("cheated_death", 0).forGetter(OrcProfile::timesCheatedDeath),
			Codec.INT.optionalFieldOf("incarnation", 0).forGetter(OrcProfile::incarnation),
			Codec.BOOL.optionalFieldOf("alive", true).forGetter(OrcProfile::isAlive),
			// Añadido en la 0.4.0: los perfiles anteriores empiezan sin nada descubierto.
			lenientList(PLAYER_INTEL).optionalFieldOf("revealed", List.of()).forGetter(NemesisCodecs::intelOf)
	).apply(i, NemesisCodecs::profile));

	private static OrcProfile profile(UUID id, OrcName name, OrcArchetype archetype, float scale, int appearance,
			List<OrcTrait> traits, List<Memory> memories, int power, int cheatedDeath, int incarnation, boolean alive,
			List<PlayerIntel> revealed) {
		Set<OrcTrait> traitSet = new HashSet<>(traits);
		Map<UUID, Set<OrcTrait>> known = new HashMap<>();
		revealed.forEach(intel -> known.computeIfAbsent(intel.player(), p -> new HashSet<>()).addAll(intel.traits()));
		return new OrcProfile(id, name, archetype, scale, appearance, traitSet, memories, power, cheatedDeath, incarnation,
				alive, known);
	}
}
