package com.oromist.worldgen.buildings;

import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.world.level.block.Blocks;

/** Choza de barro y troncos con tejado escalonado: vivienda de los soldados. */
public final class HutBuilding implements OrcBuilding {
	private static final int SIZE = 7;

	@Override
	public String id() {
		return "hut";
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
		return 7;
	}

	@Override
	public void build(BuildContext c) {
		int max = SIZE - 1;
		for (int x = 0; x <= max; x++) {
			for (int z = 0; z <= max; z++) {
				c.foundation(x, z, Palette.COBBLE);
				c.set(x, 0, z, Palette.COARSE_DIRT);
			}
		}
		c.clear(0, 1, 0, max, 6, max);

		// Paredes
		for (int y = 1; y <= 3; y++) {
			for (int i = 0; i <= max; i++) {
				c.set(i, y, 0, Palette.grimyWall(c.random()));
				c.set(i, y, max, Palette.grimyWall(c.random()));
				c.set(0, y, i, Palette.grimyWall(c.random()));
				c.set(max, y, i, Palette.grimyWall(c.random()));
			}
		}
		// Postes de esquina
		for (int[] corner : new int[][] {{0, 0}, {max, 0}, {0, max}, {max, max}}) {
			c.fill(corner[0], 1, corner[1], corner[0], 3, corner[1], Palette.LOG);
		}
		// Puerta y ventanas con barrotes
		c.clear(3, 1, 0, 3, 2, 0);
		c.set(0, 2, 3, Palette.FENCE);
		c.set(max, 2, 3, Palette.FENCE);

		// Tejado escalonado
		c.fill(0, 4, 0, max, 4, max, Palette.DARK_PLANKS);
		c.fill(1, 5, 1, max - 1, 5, max - 1, Palette.DARK_PLANKS);
		c.fill(2, 6, 2, max - 2, 6, max - 2, Blocks.DARK_OAK_SLAB.defaultBlockState());

		// Interior
		c.set(1, 1, 5, Blocks.HAY_BLOCK.defaultBlockState());
		c.set(2, 1, 5, Blocks.HAY_BLOCK.defaultBlockState());
		c.set(5, 1, 5, Blocks.BARREL.defaultBlockState());
		c.set(5, 1, 1, Blocks.LANTERN.defaultBlockState());
		if (c.random().nextBoolean()) {
			c.set(1, 1, 1, Palette.BONES);
		}

		c.spawnOrc(3, 1, 3, null, null);
	}
}
