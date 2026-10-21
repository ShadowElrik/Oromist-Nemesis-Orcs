package com.oromist.worldgen.buildings;

import com.oromist.nemesis.domain.OrcRank;
import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Tienda de guerra del jefe: lona roja y negra, trono de piedra negra y su botín.
 * Aquí aparece el capitán (o caudillo) de la aldea, un orco con nombre propio.
 */
public final class ChieftainTentBuilding implements OrcBuilding {
	private static final int SIZE = 11;
	private static final float WARCHIEF_CHANCE = 0.1f;

	@Override
	public String id() {
		return "chieftain_tent";
	}

	@Override
	public int width() {
		return SIZE;
	}

	@Override
	public int depth() {
		return SIZE;
	}

	@Override
	public int height() {
		return 8;
	}

	@Override
	public void build(BuildContext c) {
		int max = SIZE - 1;
		BlockState black = Palette.BLACK_WOOL;
		BlockState red = Palette.RED_WOOL;

		for (int x = 0; x <= max; x++) {
			for (int z = 0; z <= max; z++) {
				c.foundation(x, z, Palette.COBBLE);
				c.set(x, 0, z, Palette.DARK_PLANKS);
			}
		}
		c.clear(0, 1, 0, max, 7, max);

		// Lona a rayas
		for (int y = 1; y <= 4; y++) {
			for (int i = 0; i <= max; i++) {
				BlockState cloth = i % 2 == 0 ? black : red;
				c.set(i, y, 0, cloth);
				c.set(i, y, max, cloth);
				c.set(0, y, i, cloth);
				c.set(max, y, i, cloth);
			}
		}
		for (int[] corner : new int[][] {{0, 0}, {max, 0}, {0, max}, {max, max}}) {
			c.fill(corner[0], 1, corner[1], corner[0], 5, corner[1], Palette.DARK_LOG);
		}
		c.clear(4, 1, 0, 6, 3, 0); // entrada

		// Techo
		c.fill(0, 5, 0, max, 5, max, black);
		c.fill(1, 6, 1, max - 1, 6, max - 1, red);
		c.fill(3, 7, 3, max - 3, 7, max - 3, black);

		// Trono
		c.fill(4, 0, 7, 6, 0, 9, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
		c.set(5, 1, 8, Palette.facing(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH));
		c.set(4, 1, 9, Palette.BONES);
		c.set(6, 1, 9, Palette.BONES);
		c.set(4, 2, 9, Palette.SKULL);
		c.set(6, 2, 9, Palette.SKULL);
		c.set(2, 1, 2, Blocks.LANTERN.defaultBlockState());
		c.set(8, 1, 2, Blocks.LANTERN.defaultBlockState());
		c.chest(8, 1, 8, BuiltInLootTables.PILLAGER_OUTPOST);
		c.set(2, 1, 8, Blocks.BARREL.defaultBlockState());

		OrcRank leaderRank = c.random().nextFloat() < WARCHIEF_CHANCE ? OrcRank.WARCHIEF : OrcRank.CAPTAIN;
		c.spawnOrc(5, 1, 6, leaderRank, null);
		c.spawnOrc(3, 1, 3, null, null);
		c.spawnOrc(7, 1, 3, null, null);
	}
}
