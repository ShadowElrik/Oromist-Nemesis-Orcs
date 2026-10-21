package com.oromist.nemesis.domain;

import java.util.Objects;

/**
 * Nombre propio de un orco: un nombre ("Gorbag") y un epíteto traducible
 * ("el Rompehuesos"). El epíteto se guarda como clave de traducción para que cada
 * jugador lo vea en su idioma.
 */
public record OrcName(String firstName, String epithetKey) {
	public OrcName {
		Objects.requireNonNull(firstName, "firstName");
		epithetKey = epithetKey == null ? "" : epithetKey;
	}

	public boolean hasEpithet() {
		return !epithetKey.isEmpty();
	}
}
