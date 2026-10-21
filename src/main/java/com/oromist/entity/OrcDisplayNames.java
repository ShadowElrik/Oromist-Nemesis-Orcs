package com.oromist.entity;

import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Construye los textos visibles de un orco (nombre, rango) como componentes traducibles. */
public final class OrcDisplayNames {
	private OrcDisplayNames() {
	}

	/** "Gorbag el Cruel", coloreado según el rango. */
	public static MutableComponent fullName(OrcProfile profile) {
		MutableComponent name = profile.name().hasEpithet()
				? Component.translatable("orc.oromist-npc.full_name", profile.name().firstName(),
						Component.translatable(profile.name().epithetKey()))
				: Component.literal(profile.name().firstName());
		return name.withStyle(colorFor(profile.rank()));
	}

	public static MutableComponent rankName(OrcRank rank) {
		return Component.translatable("rank.oromist-npc." + rank.name().toLowerCase(java.util.Locale.ROOT));
	}

	public static ChatFormatting colorFor(OrcRank rank) {
		return switch (rank) {
			case GRUNT -> ChatFormatting.GRAY;
			case CAPTAIN -> ChatFormatting.RED;
			case WARCHIEF -> ChatFormatting.GOLD;
		};
	}
}
