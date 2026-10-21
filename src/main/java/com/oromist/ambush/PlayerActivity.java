package com.oromist.ambush;

import com.oromist.dialogue.DialogueTrigger;

/** Qué está haciendo un jugador cuando se evalúa una emboscada. */
public enum PlayerActivity {
	AT_HOME(DialogueTrigger.AMBUSH_HOME, "ambush.oromist-npc.home"),
	MINING(DialogueTrigger.AMBUSH_MINING, "ambush.oromist-npc.mining"),
	TRAVELING(DialogueTrigger.AMBUSH_TRAVEL, "ambush.oromist-npc.travel");

	private final DialogueTrigger dialogueTrigger;
	private final String warningKey;

	PlayerActivity(DialogueTrigger dialogueTrigger, String warningKey) {
		this.dialogueTrigger = dialogueTrigger;
		this.warningKey = warningKey;
	}

	public DialogueTrigger dialogueTrigger() {
		return dialogueTrigger;
	}

	/** Clave del aviso que ve el jugador cuando empieza la emboscada. */
	public String warningKey() {
		return warningKey;
	}
}
