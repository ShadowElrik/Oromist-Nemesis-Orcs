package com.oromist.ambush;

import com.oromist.config.OromistConfig;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.game.NemesisRuntime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;

/**
 * Decide cuándo y cómo ocurre una emboscada orca.
 *
 * <p>Periódicamente evalúa a cada jugador: detecta qué está haciendo (casa, minería,
 * viaje), tira la probabilidad configurada para esa actividad y, si toca, pide a
 * {@link AmbushSpawner} que monte el ataque, preferentemente liderado por su némesis.</p>
 */
public final class AmbushDirector {
	private static final long TICKS_PER_DAY = 24000L;

	private final NemesisRuntime runtime;
	private final AmbushSpawner spawner;
	private final List<ActivityDetector> detectors;
	private final MovementHistory history = new MovementHistory();
	private final Map<UUID, Long> lastAmbush = new HashMap<>();
	private int ticksUntilCheck;

	public AmbushDirector(NemesisRuntime runtime, AmbushSpawner spawner, List<ActivityDetector> detectors) {
		this.runtime = runtime;
		this.spawner = spawner;
		this.detectors = List.copyOf(detectors);
	}

	public void register() {
		ServerTickEvents.END_SERVER_TICK.register(this::tick);
		// En un jugador individual se pueden abrir varios mundos en la misma sesión.
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			lastAmbush.clear();
			ticksUntilCheck = 0;
		});
	}

	private void tick(MinecraftServer server) {
		OromistConfig config = runtime.config();
		if (!config.ambushesEnabled || --ticksUntilCheck > 0) {
			return;
		}
		ticksUntilCheck = Math.max(20, config.ambushCheckIntervalSeconds * 20);
		runtime.service().ifPresent(service -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				evaluate(player, service, config);
				history.record(player.getUUID(), player.position());
			}
		});
	}

	private void evaluate(ServerPlayer player, NemesisService service, OromistConfig config) {
		ServerLevel level = (ServerLevel) player.level();
		if (!canBeAmbushed(player, level, config)) {
			return;
		}
		Optional<PlayerActivity> activity = detectActivity(player);
		if (activity.isEmpty() || level.getRandom().nextFloat() >= chanceFor(activity.get(), config)) {
			return;
		}
		trigger(player, activity.get(), service);
	}

	/**
	 * Fuerza una emboscada inmediata (usado por el comando de depuración).
	 *
	 * @return {@code true} si se pudo hacer aparecer a los orcos
	 */
	public boolean trigger(ServerPlayer player, PlayerActivity activity, NemesisService service) {
		OromistConfig config = runtime.config();
		ServerLevel level = (ServerLevel) player.level();
		RandomSource random = level.getRandom();

		Optional<OrcProfile> nemesis = random.nextFloat() < config.nemesisLeadsAmbushChance
				? service.nemesisOf(player.getUUID(), profile -> true)
				: Optional.empty();
		boolean newCaptain = nemesis.isEmpty() && random.nextFloat() < 0.3f;
		int grunts = config.ambushMinGrunts + random.nextInt(Math.max(1, config.ambushMaxGrunts - config.ambushMinGrunts + 1));

		boolean spawned = !spawner.spawn(level, player, activity, service, nemesis, newCaptain, grunts).isEmpty();
		if (spawned) {
			lastAmbush.put(player.getUUID(), level.getGameTime());
		}
		return spawned;
	}

	public Optional<PlayerActivity> detectActivity(ServerPlayer player) {
		return detectors.stream()
				.map(detector -> detector.detect(player, history))
				.flatMap(Optional::stream)
				.findFirst();
	}

	private boolean canBeAmbushed(ServerPlayer player, ServerLevel level, OromistConfig config) {
		if (player.isCreative() || player.isSpectator() || !player.isAlive() || level.getDifficulty() == Difficulty.PEACEFUL) {
			return false;
		}
		long now = level.getGameTime();
		if (now < config.ambushGraceDays * TICKS_PER_DAY) {
			return false;
		}
		Long last = lastAmbush.get(player.getUUID());
		long cooldown = config.ambushCooldownMinutes * 60L * 20L;
		return last == null || now - last >= cooldown;
	}

	private static float chanceFor(PlayerActivity activity, OromistConfig config) {
		return switch (activity) {
			case AT_HOME -> config.ambushChanceAtHome;
			case MINING -> config.ambushChanceMining;
			case TRAVELING -> config.ambushChanceTraveling;
		};
	}
}
