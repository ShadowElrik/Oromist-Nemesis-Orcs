package com.oromist.nemesis.game;

import com.oromist.config.OromistConfig;
import com.oromist.nemesis.domain.NemesisListener;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcNameGenerator;
import com.oromist.nemesis.domain.TraitRoller;
import com.oromist.nemesis.persistence.NemesisSavedData;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

/**
 * Ciclo de vida del sistema Némesis: crea el {@link NemesisService} cuando arranca un
 * servidor (con los datos guardados de ese mundo) y lo descarta al cerrarse.
 */
public final class NemesisRuntime {
	private final OromistConfig config;
	private final Function<MinecraftServer, NemesisListener> listenerFactory;
	private final ActiveOrcs activeOrcs = new ActiveOrcs();
	private @Nullable NemesisService service;
	private @Nullable MinecraftServer server;

	public NemesisRuntime(OromistConfig config, Function<MinecraftServer, NemesisListener> listenerFactory) {
		this.config = config;
		this.listenerFactory = listenerFactory;
	}

	public void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(this::start);
		ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> {
			service = null;
			server = null;
			activeOrcs.clear();
		});
	}

	private void start(MinecraftServer startedServer) {
		NemesisService created = new NemesisService(
				NemesisSavedData.get(startedServer),
				config.toRules(),
				new OrcNameGenerator.Syllabic(),
				new TraitRoller.Default(),
				new SplittableRandom());
		created.addListener(listenerFactory.apply(startedServer));
		this.service = created;
		this.server = startedServer;
	}

	/** Servicio activo, vacío si no hay un mundo cargado. */
	public Optional<NemesisService> service() {
		return Optional.ofNullable(service);
	}

	public Optional<MinecraftServer> server() {
		return Optional.ofNullable(server);
	}

	/** Orcos con perfil cuya entidad está cargada y activa en este momento. */
	public ActiveOrcs activeOrcs() {
		return activeOrcs;
	}

	public OromistConfig config() {
		return config;
	}
}
