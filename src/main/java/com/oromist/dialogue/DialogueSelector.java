package com.oromist.dialogue;

import com.oromist.nemesis.domain.OrcProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Estrategia que elige qué dice un orco a un jugador en un momento dado. */
public interface DialogueSelector {
	Optional<DialogueLine> select(OrcProfile speaker, UUID playerId, String playerName, DialogueTrigger trigger,
			long gameTime, RandomGenerator random);
}
