package com.oromist.dialogue;

import java.util.List;

/**
 * Una frase lista para mostrarse: una clave de traducción y sus argumentos.
 * Es independiente de Minecraft; {@code ChatDialogueSpeaker} la convierte en texto del juego.
 */
public record DialogueLine(String translationKey, List<Arg> args) {
	public DialogueLine {
		args = List.copyOf(args);
	}

	/** Argumento de una frase. */
	public sealed interface Arg permits Literal, Translatable, Number {
	}

	/** Texto literal, ej. el nombre del jugador. */
	public record Literal(String text) implements Arg {
	}

	/** Texto a traducir, ej. el nombre de un bioma. */
	public record Translatable(String key) implements Arg {
	}

	/** Número, ej. "hace 3 días". */
	public record Number(long value) implements Arg {
	}
}
