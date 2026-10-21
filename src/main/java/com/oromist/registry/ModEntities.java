package com.oromist.registry;

import com.oromist.OromistNPC;
import com.oromist.entity.OrcEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

/** Registro de los tipos de entidad del mod. */
public final class ModEntities {
	public static final ResourceKey<EntityType<?>> ORC_KEY = ResourceKey.create(Registries.ENTITY_TYPE, OromistNPC.id("orc"));

	public static final EntityType<OrcEntity> ORC = FabricEntityType.Builder.createMob(OrcEntity::new, MobCategory.MONSTER, mob -> mob
					.defaultAttributes(OrcEntity::createAttributes)
					.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules))
			// Tamaño base de un humanoide; la altura real escala con el atributo SCALE de cada orco.
			.sized(0.6F, 1.95F)
			.eyeHeight(1.74F)
			.clientTrackingRange(10)
			.build(ORC_KEY);

	private ModEntities() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, ORC_KEY, ORC);
	}
}
