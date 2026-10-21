package com.oromist.ambush;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Reconoce una actividad concreta del jugador. Las implementaciones son independientes
 * entre sí; {@link #DEFAULTS} define el orden de prioridad (la primera que coincida gana).
 */
public interface ActivityDetector {
	Optional<PlayerActivity> detect(ServerPlayer player, MovementHistory history);

	List<ActivityDetector> DEFAULTS = List.of(new Mining(), new AtHome(), new Traveling());

	/** Bajo tierra, sin ver el cielo y bastante por debajo del nivel del mar. */
	final class Mining implements ActivityDetector {
		private static final int DEPTH_BELOW_SEA_LEVEL = 10;

		@Override
		public Optional<PlayerActivity> detect(ServerPlayer player, MovementHistory history) {
			BlockPos pos = player.blockPosition();
			boolean deep = pos.getY() < player.level().getSeaLevel() - DEPTH_BELOW_SEA_LEVEL;
			boolean covered = !player.level().canSeeSky(pos);
			return deep && covered ? Optional.of(PlayerActivity.MINING) : Optional.empty();
		}
	}

	/** Cerca de su punto de reaparición (cama o ancla) en la misma dimensión. */
	final class AtHome implements ActivityDetector {
		private static final double HOME_RADIUS = 48.0;

		@Override
		public Optional<PlayerActivity> detect(ServerPlayer player, MovementHistory history) {
			ServerPlayer.RespawnConfig respawn = player.getRespawnConfig();
			if (respawn == null || !respawn.respawnData().dimension().equals(player.level().dimension())) {
				return Optional.empty();
			}
			BlockPos home = respawn.respawnData().pos();
			double dx = home.getX() - player.getX();
			double dz = home.getZ() - player.getZ();
			return dx * dx + dz * dz <= HOME_RADIUS * HOME_RADIUS ? Optional.of(PlayerActivity.AT_HOME) : Optional.empty();
		}
	}

	/** Ha recorrido una buena distancia desde la última evaluación (a pie, a caballo, en barco...). */
	final class Traveling implements ActivityDetector {
		private static final double MIN_DISTANCE = 60.0;

		@Override
		public Optional<PlayerActivity> detect(ServerPlayer player, MovementHistory history) {
			Optional<Vec3> previous = history.previousPosition(player.getUUID());
			boolean moved = previous.map(p -> horizontal(p, player.position()) >= MIN_DISTANCE).orElse(false);
			return moved || player.isPassenger() ? Optional.of(PlayerActivity.TRAVELING) : Optional.empty();
		}

		private static double horizontal(Vec3 a, Vec3 b) {
			double dx = a.x - b.x;
			double dz = a.z - b.z;
			return Math.sqrt(dx * dx + dz * dz);
		}
	}
}
