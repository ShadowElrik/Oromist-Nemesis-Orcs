package com.oromist.worldgen;

/**
 * Plano de un edificio de la aldea orca. Cada edificio declara su tamaño y sabe
 * construirse sobre un {@link BuildContext}; no conoce chunks, rotaciones ni guardado.
 */
public interface OrcBuilding {
	/** Identificador único (se guarda en el mundo; no cambiarlo en edificios existentes). */
	String id();

	/** Ancho (eje x local). */
	int width();

	/** Profundidad (eje z local). */
	int depth();

	/** Altura total, incluido el suelo. */
	int height();

	void build(BuildContext context);
}
