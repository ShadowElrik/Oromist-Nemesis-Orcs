package com.oromist.ambush;

import com.oromist.dialogue.game.DialogueSpeaker;
import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.game.Encounters;
import com.oromist.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;

/**
 * Materializa una emboscada: hace aparecer al líder (el némesis del jugador o un
 * capitán nuevo) y a sus soldados, los pone a cazar al jugador y anuncia el ataque.
 *
 * <p>En los caminos la escolta va a caballo (jinetes con lanza) y a veces la acompaña un chamán.</p>
 */
public final class AmbushSpawner {
	/** Jinetes que acompañan a una emboscada de viaje. */
	private static final int TRAVEL_RIDERS = 2;
	private static final float SHAMAN_ESCORT_CHANCE = 0.3f;

	private final DialogueSpeaker speaker;

	public AmbushSpawner(DialogueSpeaker speaker) {
		this.speaker = speaker;
	}

	/**
	 * @param nemesis perfil que liderará la emboscada; vacío para un capitán nuevo o solo soldados
	 * @return los orcos creados (vacío si no se encontró sitio)
	 */
	public List<OrcEntity> spawn(ServerLevel level, ServerPlayer player, PlayerActivity activity, NemesisService service,
			Optional<OrcProfile> nemesis, boolean newCaptain, int grunts) {
		RandomSource random = level.getRandom();
		SpawnLocator locator = SpawnLocator.forActivity(activity);
		Optional<BlockPos> anchor = locator.find(level, player, random);
		if (anchor.isEmpty()) {
			return List.of();
		}

		List<OrcEntity> party = new ArrayList<>();
		OrcEntity leader = createOrc(level, player, anchor.get());
		if (leader == null) {
			return List.of();
		}
		if (nemesis.isPresent()) {
			leader.bindProfile(nemesis.get(), service.beginIncarnation(nemesis.get()), true);
			service.onAmbush(nemesis.get(), Encounters.of(player));
		} else if (newCaptain) {
			leader.setPendingRank(OrcRank.CAPTAIN);
			OrcProfile profile = com.oromist.nemesis.game.OrcProfiles.ensure(leader, service);
			service.onAmbush(profile, Encounters.of(player));
		}
		party.add(leader);

		for (int i = 0; i < grunts; i++) {
			BlockPos near = locator.find(level, player, random).orElse(anchor.get());
			OrcEntity grunt = createOrc(level, player, near);
			if (grunt != null) {
				escortRole(activity, i, random).ifPresent(role -> grunt.prepareAs(role, random));
				party.add(grunt);
			}
		}

		party.forEach(level::addFreshEntityWithPassengers);
		announce(level, player, activity);
		if (leader.profileId().isPresent()) {
			speaker.speak(leader, player, activity.dialogueTrigger());
		}
		return party;
	}

	/** Papel especial de un miembro de la escolta, o vacío para dejar su arquetipo al azar. */
	private static Optional<OrcArchetype> escortRole(PlayerActivity activity, int index, RandomSource random) {
		if (activity == PlayerActivity.TRAVELING && index < TRAVEL_RIDERS) {
			return Optional.of(OrcArchetype.LANCER);
		}
		if (index == TRAVEL_RIDERS && random.nextFloat() < SHAMAN_ESCORT_CHANCE) {
			return Optional.of(OrcArchetype.SHAMAN);
		}
		return Optional.empty();
	}

	private static OrcEntity createOrc(ServerLevel level, ServerPlayer target, BlockPos pos) {
		OrcEntity orc = ModEntities.ORC.create(level, EntitySpawnReason.EVENT);
		if (orc == null) {
			return null;
		}
		float yaw = level.getRandom().nextFloat() * 360.0F;
		orc.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
		orc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
		orc.setTarget(target);
		return orc;
	}

	private static void announce(ServerLevel level, ServerPlayer player, PlayerActivity activity) {
		player.sendSystemMessage(Component.translatable(activity.warningKey()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAID_HORN, SoundSource.HOSTILE, 0.6F, 0.8F);
	}
}
