package com.oromist.dialogue;

/** Momento de la partida que provoca que un orco hable. */
public enum DialogueTrigger {
	/** El orco ve al jugador al inicio de un combate. Usa sus recuerdos. */
	SIGHTED,
	/** El orco acaba de matar al jugador. */
	KILLED_PLAYER,
	/** El orco huye malherido. */
	FLEEING,
	/** El orco ha ascendido de rango. */
	PROMOTED,
	/** Emboscada mientras el jugador está en casa. */
	AMBUSH_HOME,
	/** Emboscada mientras el jugador mina. */
	AMBUSH_MINING,
	/** Emboscada mientras el jugador viaja. */
	AMBUSH_TRAVEL,
	/** Últimas palabras de un orco que de verdad muere. */
	DYING,
	/** Un jugador acaba de matar la montura del orco. */
	MOUNT_LOST,
	/** Un orco malherido cede al interrogatorio y delata a otro. */
	INTERROGATION_TALK,
	/** Un orco con nombre se niega a hablar. */
	INTERROGATION_REFUSE
}
