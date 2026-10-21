package com.oromist;

import com.oromist.ambush.ActivityDetector;
import com.oromist.ambush.AmbushDirector;
import com.oromist.ambush.AmbushSpawner;
import com.oromist.command.NemesisCommand;
import com.oromist.config.ConfigLoader;
import com.oromist.config.OromistConfig;
import com.oromist.dialogue.MemoryDialogueSelector;
import com.oromist.dialogue.game.ChatDialogueSpeaker;
import com.oromist.dialogue.game.DialogueSpeaker;
import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcSizes;
import com.oromist.nemesis.game.NemesisAnnouncer;
import com.oromist.nemesis.game.NemesisCombatEvents;
import com.oromist.nemesis.game.NemesisRuntime;
import com.oromist.nemesis.game.OrcNemesisHooks;
import com.oromist.registry.ModEntities;
import com.oromist.registry.ModItems;
import com.oromist.registry.ModStructures;
import com.oromist.worldgen.OrcSpawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del mod y "raíz de composición": aquí (y solo aquí) se crean los
 * objetos concretos y se conectan entre sí. El resto de clases reciben sus
 * dependencias, lo que permite sustituir piezas (otro selector de diálogos, otro
 * guardado...) sin tocar el código que las usa.
 */
public class OromistNPC implements ModInitializer {
	public static final String MOD_ID = "oromist-npc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		OromistConfig config = ConfigLoader.load(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json"), LOGGER);
		OrcSizes.configure(config.minOrcScale, config.maxOrcScale);

		// Contenido del juego
		ModEntities.register();
		ModItems.register();
		ModStructures.register();
		OrcSpawns.register();

		// Sistema Némesis
		NemesisRuntime runtime = new NemesisRuntime(config, NemesisAnnouncer::new);
		runtime.register();
		DialogueSpeaker speaker = new ChatDialogueSpeaker(runtime, new MemoryDialogueSelector());
		OrcEntity.installHooks(new OrcNemesisHooks(runtime, speaker));
		new NemesisCombatEvents(runtime, speaker).register();

		// Emboscadas
		AmbushDirector ambushes = new AmbushDirector(runtime, new AmbushSpawner(speaker), ActivityDetector.DEFAULTS);
		ambushes.register();

		new NemesisCommand(runtime, ambushes).register();
		LOGGER.info("Oromist: los orcos recuerdan.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
