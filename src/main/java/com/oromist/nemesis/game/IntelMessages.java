package com.oromist.nemesis.game;

import com.oromist.entity.OrcDisplayNames;
import com.oromist.nemesis.domain.IntelService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Textos de "información de guerra" que se muestran al jugador. */
public final class IntelMessages {
	private IntelMessages() {
	}

	/** El jugador lo vio con sus propios ojos en combate. */
	public static Component discovered(OrcProfile subject, OrcTrait trait) {
		return tagged(Component.translatable("intel.oromist-npc.discovered",
				OrcDisplayNames.fullName(subject), OrcDisplayNames.traitName(trait)));
	}

	/** Un orco interrogado delata a otro. */
	public static Component informant(IntelService.Intel intel) {
		return tagged(Component.translatable("intel.oromist-npc.informant",
				OrcDisplayNames.fullName(intel.subject()), OrcDisplayNames.rankName(intel.subject().rank()),
				OrcDisplayNames.traitName(intel.trait())));
	}

	/** Lo que dicen unas notas de guerra. */
	public static Component fromNotes(IntelService.Intel intel) {
		return tagged(Component.translatable("intel.oromist-npc.notes",
				OrcDisplayNames.fullName(intel.subject()), OrcDisplayNames.rankName(intel.subject().rank()),
				OrcDisplayNames.traitName(intel.trait())));
	}

	public static Component nothingNew() {
		return tagged(Component.translatable("intel.oromist-npc.nothing_new"));
	}

	/** Pista para que el jugador sepa que puede interrogar a un orco malherido. */
	public static Component interrogationHint() {
		return Component.translatable("intel.oromist-npc.hint").withStyle(ChatFormatting.YELLOW);
	}

	/** "[Información] mensaje": la etiqueta en negrita y el mensaje en gris, sin heredar estilos. */
	private static Component tagged(MutableComponent message) {
		return Component.empty()
				.append(Component.translatable("intel.oromist-npc.prefix").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
				.append(Component.literal(" "))
				.append(message.withStyle(ChatFormatting.GRAY));
	}
}
