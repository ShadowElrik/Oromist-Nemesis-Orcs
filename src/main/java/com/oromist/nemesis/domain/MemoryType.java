package com.oromist.nemesis.domain;

/**
 * Tipos de eventos que un orco puede recordar sobre un jugador.
 *
 * <p>Cada tipo aporta un peso de "rencor" (para decidir quién es el némesis de
 * quién) y una prioridad de diálogo (qué recuerdo menciona primero al hablar).</p>
 */
public enum MemoryType {
	/** Primera vez que el orco vio al jugador. */
	FIRST_ENCOUNTER(1, 0),
	/** El jugador hirió gravemente al orco. */
	WOUNDED_BY_PLAYER(2, 2),
	/** El orco huyó del jugador y sobrevivió. */
	FLED_FROM_PLAYER(3, 3),
	/** El jugador huyó del orco. */
	PLAYER_FLED(2, 4),
	/** El jugador mató a un compañero del orco delante suyo. */
	PLAYER_KILLED_ALLY(2, 5),
	/** El orco emboscó al jugador. */
	AMBUSHED_PLAYER(1, 1),
	/** El orco mató al jugador. */
	KILLED_PLAYER(5, 8),
	/** El jugador "mató" al orco, pero este sobrevivió y volvió. */
	SURVIVED_DEATH(6, 10);

	private final int grudgeWeight;
	private final int dialoguePriority;

	MemoryType(int grudgeWeight, int dialoguePriority) {
		this.grudgeWeight = grudgeWeight;
		this.dialoguePriority = dialoguePriority;
	}

	public int grudgeWeight() {
		return grudgeWeight;
	}

	public int dialoguePriority() {
		return dialoguePriority;
	}
}
