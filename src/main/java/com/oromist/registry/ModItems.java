package com.oromist.registry;

import com.oromist.OromistNPC;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/** Registro de objetos del mod. */
public final class ModItems {
	public static final ResourceKey<Item> ORC_SPAWN_EGG_KEY = ResourceKey.create(Registries.ITEM, OromistNPC.id("orc_spawn_egg"));
	/** Clave de la pestaña vanilla de huevos (se construye a mano: la constante vanilla no es pública). */
	private static final ResourceKey<CreativeModeTab> SPAWN_EGGS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("spawn_eggs"));

	public static Item ORC_SPAWN_EGG;

	private ModItems() {
	}

	public static void register() {
		ORC_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, ORC_SPAWN_EGG_KEY,
				new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.ORC).setId(ORC_SPAWN_EGG_KEY)));
		CreativeModeTabEvents.modifyOutputEvent(SPAWN_EGGS_TAB).register(output -> output.accept(ORC_SPAWN_EGG));
	}
}
