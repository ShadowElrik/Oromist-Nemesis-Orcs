package com.oromist.gametest;

import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.registry.ModEntities;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;

/** Utilidades comunes de las pruebas en el juego: suelo, dificultad y orcos de un arquetipo concreto. */
final class TestArena {
	/** Tamaño de la estructura vacía por defecto de Fabric (8×8). */
	static final int SIZE = 8;

	private TestArena() {
	}

	/** Suelo de piedra y dificultad normal (en pacífico los monstruos desaparecen al instante). */
	static void prepare(GameTestHelper helper) {
		helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
		for (int x = 0; x < SIZE; x++) {
			for (int z = 0; z < SIZE; z++) {
				helper.setBlock(x, 0, z, Blocks.STONE);
			}
		}
	}

	static OrcEntity orc(GameTestHelper helper, OrcArchetype archetype, int x, int z) {
		OrcEntity orc = helper.spawn(ModEntities.ORC, x, 1, z);
		orc.prepareAs(archetype, orc.getRandom());
		return orc;
	}
}
