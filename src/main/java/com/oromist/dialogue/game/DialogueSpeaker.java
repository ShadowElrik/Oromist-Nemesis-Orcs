package com.oromist.dialogue.game;

import com.oromist.dialogue.DialogueTrigger;
import com.oromist.entity.OrcEntity;
import net.minecraft.server.level.ServerPlayer;

/** Hace hablar a un orco. La implementación decide el canal (chat, título, burbuja...). */
public interface DialogueSpeaker {
	/** El orco le dice algo a {@code target}; lo escuchan también los jugadores cercanos. */
	void speak(OrcEntity speaker, ServerPlayer target, DialogueTrigger trigger);
}
