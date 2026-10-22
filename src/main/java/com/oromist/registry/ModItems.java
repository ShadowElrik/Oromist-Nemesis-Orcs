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
	public static final ResourceKey<Item> WAR_NOTES_KEY = ResourceKey.create(Registries.ITEM, OromistNPC.id("war_notes"));
	/** Clave de la pestaña vanilla de huevos (se construye a mano: la constante vanilla no es pública). */
	private static final ResourceKey<CreativeModeTab> SPAWN_EGGS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("spawn_eggs"));
	private static final ResourceKey<CreativeModeTab> TOOLS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"));

	public static Item ORC_SPAWN_EGG;
	/**
	 * Notas de guerra: pergaminos orcos que revelan un rasgo de un capitán al leerlos.
	 * Es un objeto sin comportamiento propio; lo que hace al usarlo está en {@code IntelEvents}.
	 */
	public static Item WAR_NOTES;

	private ModItems() {
	}

	public static void register() {
		ORC_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, ORC_SPAWN_EGG_KEY,
				new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.ORC).setId(ORC_SPAWN_EGG_KEY)));
		CreativeModeTabEvents.modifyOutputEvent(SPAWN_EGGS_TAB).register(output -> output.accept(ORC_SPAWN_EGG));

		WAR_NOTES = Registry.register(BuiltInRegistries.ITEM, WAR_NOTES_KEY,
				new Item(new Item.Properties().stacksTo(16).setId(WAR_NOTES_KEY)));
		CreativeModeTabEvents.modifyOutputEvent(TOOLS_TAB).register(output -> output.accept(WAR_NOTES));
	}
}
