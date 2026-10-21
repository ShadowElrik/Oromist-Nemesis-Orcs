package com.oromist.nemesis.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcName;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.UUIDUtil;

/**
 * Codecs de serialización del dominio. Están separados del dominio para que este
 * no dependa de la librería de serialización del juego.
 */
public final class NemesisCodecs {
	private NemesisCodecs() {
	}

	public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> type) {
		return Codec.STRING.xmap(
				name -> Enum.valueOf(type, name.toUpperCase(Locale.ROOT)),
				value -> value.name().toLowerCase(Locale.ROOT));
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

	public static final Codec<OrcProfile> PROFILE = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(OrcProfile::id),
			NAME.fieldOf("name").forGetter(OrcProfile::name),
			ARCHETYPE.fieldOf("archetype").forGetter(OrcProfile::archetype),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(OrcProfile::scale),
			TRAIT.listOf().optionalFieldOf("traits", List.of()).forGetter(p -> List.copyOf(p.traits())),
			MEMORY.listOf().optionalFieldOf("memories", List.of()).forGetter(OrcProfile::memories),
			Codec.INT.optionalFieldOf("power", 0).forGetter(OrcProfile::power),
			Codec.INT.optionalFieldOf("cheated_death", 0).forGetter(OrcProfile::timesCheatedDeath),
			Codec.INT.optionalFieldOf("incarnation", 0).forGetter(OrcProfile::incarnation),
			Codec.BOOL.optionalFieldOf("alive", true).forGetter(OrcProfile::isAlive)
	).apply(i, NemesisCodecs::profile));

	private static OrcProfile profile(java.util.UUID id, OrcName name, OrcArchetype archetype, float scale,
			List<OrcTrait> traits, List<Memory> memories, int power, int cheatedDeath, int incarnation, boolean alive) {
		Set<OrcTrait> traitSet = new HashSet<>(traits);
		return new OrcProfile(id, name, archetype, scale, traitSet, memories, power, cheatedDeath, incarnation, alive);
	}
}
