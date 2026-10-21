package com.oromist.worldgen;

import com.oromist.OromistNPC;
import com.oromist.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

/**
 * Aparición natural de pequeñas partidas de orcos por la noche en los biomas donde
 * viven (tag {@code oromist-npc:orc_spawns}, editable mediante datapacks).
 */
public final class OrcSpawns {
	public static final TagKey<Biome> ORC_SPAWN_BIOMES = TagKey.create(Registries.BIOME, OromistNPC.id("orc_spawns"));

	private static final int WEIGHT = 12;
	private static final int MIN_GROUP = 1;
	private static final int MAX_GROUP = 3;

	private OrcSpawns() {
	}

	public static void register() {
		BiomeModifications.addSpawn(BiomeSelectors.tag(ORC_SPAWN_BIOMES), MobCategory.MONSTER, ModEntities.ORC,
				WEIGHT, MIN_GROUP, MAX_GROUP);
	}
}
