package com.oromist.nemesis.game;

import com.oromist.entity.OrcDisplayNames;
import com.oromist.nemesis.domain.NemesisListener;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Avisa a los jugadores que un orco conoce de los cambios en el ejército orco
 * (ascensos, muertes definitivas), como en el panel del sistema Némesis original.
 */
public final class NemesisAnnouncer implements NemesisListener {
	private final MinecraftServer server;

	public NemesisAnnouncer(MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void onPromoted(OrcProfile profile, OrcRank newRank) {
		if (newRank.isNamed()) {
			tellAcquaintances(profile, Component.translatable("announce.oromist-npc.promoted",
					OrcDisplayNames.fullName(profile), OrcDisplayNames.rankName(newRank)).withStyle(ChatFormatting.DARK_RED));
		}
	}

	@Override
	public void onDied(OrcProfile profile, UUID playerId) {
		if (profile.rank().isNamed()) {
			tellAcquaintances(profile, Component.translatable("announce.oromist-npc.died",
					OrcDisplayNames.fullName(profile)).withStyle(ChatFormatting.DARK_GREEN));
		}
	}

	private void tellAcquaintances(OrcProfile profile, Component message) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (profile.knows(player.getUUID())) {
				player.sendSystemMessage(message);
			}
		}
	}
}
