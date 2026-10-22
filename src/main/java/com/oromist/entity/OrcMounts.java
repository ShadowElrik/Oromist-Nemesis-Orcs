package com.oromist.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.ZombieHorse;

/**
 * Monturas de los jinetes orcos: caballos zombi que "no se cansan ni sienten miedo,
 * porque ya están muertos".
 *
 * <p>La montura se invoca la primera vez que el jinete está en el mundo, sea cual sea la forma
 * en que apareció (natural, aldea, emboscada, huevo o un némesis que vuelve). Si la montura
 * muere, no se repone: el orco sigue a pie y lo recuerda.</p>
 */
public final class OrcMounts {
	/** Etiqueta que distingue a las monturas orcas de los caballos zombi normales. */
	public static final String MOUNT_TAG = "oromist_orc_mount";

	private OrcMounts() {
	}

	public static boolean isOrcMount(Entity entity) {
		return entity instanceof ZombieHorse && entity.entityTags().contains(MOUNT_TAG);
	}

	/**
	 * Crea un caballo zombi bajo el jinete si hay sitio para él.
	 *
	 * @return la montura, o vacío si no cabía (por ejemplo, en un túnel de mina)
	 */
	public static Optional<ZombieHorse> summonFor(OrcEntity rider, ServerLevel level) {
		if (!level.noCollision(EntityTypes.ZOMBIE_HORSE.getSpawnAABB(rider.getX(), rider.getY(), rider.getZ()))) {
			return Optional.empty();
		}
		// JOCKEY: con NATURAL el caballo zombi vanilla traería su propio zombi jinete.
		ZombieHorse horse = EntityTypes.ZOMBIE_HORSE.create(level, EntitySpawnReason.JOCKEY);
		if (horse == null) {
			return Optional.empty();
		}
		horse.snapTo(rider.getX(), rider.getY(), rider.getZ(), rider.getYRot(), 0.0F);
		horse.finalizeSpawn(level, level.getCurrentDifficultyAt(rider.blockPosition()), EntitySpawnReason.JOCKEY, null);
		horse.addTag(MOUNT_TAG);
		if (rider.isPersistenceRequired()) {
			horse.setPersistenceRequired();
		}
		level.addFreshEntity(horse);
		rider.startRiding(horse, false, false);
		return Optional.of(horse);
	}
}
