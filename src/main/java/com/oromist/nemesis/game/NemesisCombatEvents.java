package com.oromist.nemesis.game;

import com.oromist.dialogue.DialogueTrigger;
import com.oromist.dialogue.game.DialogueSpeaker;
import com.oromist.entity.OrcDisplayNames;
import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcMounts;
import com.oromist.entity.OrcSquad;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Escucha muertes y heridas para alimentar la memoria de los orcos:
 * <ul>
 *   <li>Un orco mata a un jugador → lo recuerda, asciende y se burla.</li>
 *   <li>Un jugador mata a un orco con perfil → muere o engaña a la muerte.</li>
 *   <li>Un jugador mata a un orco delante de otros → los testigos lo recuerdan.</li>
 *   <li>Un jugador hiere gravemente a un orco con perfil → lo recuerda.</li>
 *   <li>Un jugador mata la montura de un jinete → el jinete lo recuerda.</li>
 * </ul>
 *
 * <p>Si un chamán presencia la muerte de un capitán, este tiene más probabilidad de volver.
 * Un capitán o caudillo que muere de verdad deja caer unas notas de guerra.</p>
 */
public final class NemesisCombatEvents {
	private static final double WITNESS_RADIUS = 16.0;
	/** Probabilidad de que un soldado anónimo que presencia una muerte se vuelva "notable". */
	private static final float WITNESS_BECOMES_NOTABLE = 0.25f;
	private static final float SERIOUS_WOUND_RATIO = 0.3f;

	private final NemesisRuntime runtime;
	private final DialogueSpeaker speaker;

	public NemesisCombatEvents(NemesisRuntime runtime, DialogueSpeaker speaker) {
		this.runtime = runtime;
		this.speaker = speaker;
	}

	public void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register(this::afterDeath);
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> afterDamage(entity, source, taken));
	}

	private void afterDeath(LivingEntity entity, DamageSource source) {
		runtime.service().ifPresent(service -> {
			if (entity instanceof ServerPlayer player && attackerOrc(source) instanceof OrcEntity orc) {
				onOrcKilledPlayer(service, orc, player);
			} else if (entity instanceof OrcEntity orc && playerAttacker(source) instanceof ServerPlayer player) {
				onPlayerKilledOrc(service, orc, player);
			} else if (OrcMounts.isOrcMount(entity) && entity.getFirstPassenger() instanceof OrcEntity rider
					&& playerAttacker(source) instanceof ServerPlayer player) {
				onMountKilled(service, rider, player);
			}
		});
	}

	private void onOrcKilledPlayer(NemesisService service, OrcEntity orc, ServerPlayer player) {
		OrcProfile profile = OrcProfiles.ensure(orc, service);
		service.onOrcKilledPlayer(profile, Encounters.of(player));
		orc.applyProfile(profile);
		speaker.speak(orc, player, DialogueTrigger.KILLED_PLAYER);
	}

	private void onPlayerKilledOrc(NemesisService service, OrcEntity orc, ServerPlayer player) {
		OrcProfiles.of(orc, service).ifPresent(profile -> {
			boolean shamanNearby = OrcSquad.hasShamanNearby(orc);
			NemesisService.DeathOutcome outcome = service.onOrcSlainByPlayer(profile, Encounters.of(player), shamanNearby);
			if (outcome == NemesisService.DeathOutcome.DIED) {
				speaker.speak(orc, player, DialogueTrigger.DYING);
				if (profile.rank().isNamed() && orc.level() instanceof ServerLevel level) {
					orc.spawnAtLocation(level, ModItems.WAR_NOTES);
				}
			} else if (shamanNearby) {
				player.sendSystemMessage(Component.translatable("announce.oromist-npc.shaman_ritual",
						OrcDisplayNames.fullName(profile)).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
			}
		});
		notifyWitnesses(service, orc, player);
	}

	private void onMountKilled(NemesisService service, OrcEntity rider, ServerPlayer player) {
		OrcProfile profile = OrcProfiles.ensure(rider, service);
		service.onMountKilled(profile, Encounters.of(player));
		speaker.speak(rider, player, DialogueTrigger.MOUNT_LOST);
	}

	private void notifyWitnesses(NemesisService service, OrcEntity victim, ServerPlayer player) {
		List<OrcEntity> witnesses = victim.level().getEntitiesOfClass(OrcEntity.class,
				victim.getBoundingBox().inflate(WITNESS_RADIUS), orc -> orc != victim && orc.isAlive());
		for (OrcEntity witness : witnesses) {
			boolean notable = witness.profileId().isPresent() || witness.getRandom().nextFloat() < WITNESS_BECOMES_NOTABLE;
			if (notable) {
				service.onAllyKilledByPlayer(OrcProfiles.ensure(witness, service), Encounters.of(player));
			}
		}
	}

	private void afterDamage(LivingEntity entity, DamageSource source, float taken) {
		if (!(entity instanceof OrcEntity orc) || !orc.isAlive() || !(playerAttacker(source) instanceof ServerPlayer player)) {
			return;
		}
		if (orc.isBroken() && !orc.wasInterrogated() && !orc.currentRank().isNamed()) {
			player.sendSystemMessage(IntelMessages.interrogationHint(), true);
		}
		if (taken >= orc.getMaxHealth() * SERIOUS_WOUND_RATIO) {
			runtime.service().ifPresent(service ->
					OrcProfiles.of(orc, service).ifPresent(profile -> service.onWoundedByPlayer(profile, Encounters.of(player))));
		}
	}

	/** El orco responsable, aunque haya matado con una flecha. */
	private static Entity attackerOrc(DamageSource source) {
		Entity attacker = source.getEntity();
		if (attacker == null && source.getDirectEntity() instanceof Projectile projectile) {
			attacker = projectile.getOwner();
		}
		return attacker;
	}

	private static Entity playerAttacker(DamageSource source) {
		return source.getEntity();
	}
}
