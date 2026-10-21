package com.oromist.worldgen.buildings;

import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/** Barracón alargado de piedra y madera donde duerme la tropa. Guarda un cofre con botín. */
public final class LonghouseBuilding implements OrcBuilding {
	private static final int WIDTH = 9;
	private static final int DEPTH = 13;

	@Override
	public String id() {
		return "longhouse";
	}

	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int depth() {
		return DEPTH;
	}

	@Override
	public int height() {
		return 8;
	}

	@Override
	public void build(BuildContext c) {
		int maxX = WIDTH - 1;
		int maxZ = DEPTH - 1;
		for (int x = 0; x <= maxX; x++) {
			for (int z = 0; z <= maxZ; z++) {
				c.foundation(x, z, Palette.COBBLE);
				c.set(x, 0, z, Palette.stoneFloor(c.random()));
			}
		}
		c.clear(0, 1, 0, maxX, 7, maxZ);

		for (int y = 1; y <= 4; y++) {
			for (int x = 0; x <= maxX; x++) {
				c.set(x, y, 0, Palette.grimyWall(c.random()));
				c.set(x, y, maxZ, Palette.grimyWall(c.random()));
			}
			for (int z = 0; z <= maxZ; z++) {
				boolean pillar = z % 4 == 0;
				c.set(0, y, z, pillar ? Palette.LOG : Palette.grimyWall(c.random()));
				c.set(maxX, y, z, pillar ? Palette.LOG : Palette.grimyWall(c.random()));
			}
		}
		// Entrada doble y ventanas
		c.clear(3, 1, 0, 5, 3, 0);
		for (int z = 2; z < maxZ; z += 4) {
			c.set(0, 3, z, Palette.FENCE);
			c.set(maxX, 3, z, Palette.FENCE);
		}

		// Tejado
		c.fill(0, 5, 0, maxX, 5, maxZ, Palette.DARK_PLANKS);
		c.fill(1, 6, 0, maxX - 1, 6, maxZ, Palette.DARK_PLANKS);
		c.fill(3, 7, 0, maxX - 3, 7, maxZ, Blocks.DARK_OAK_SLAB.defaultBlockState());

		// Mesa larga central
		for (int z = 4; z <= 9; z++) {
			c.set(4, 1, z, Palette.FENCE);
			c.set(4, 2, z, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
		}
		c.set(1, 1, 10, Blocks.BARREL.defaultBlockState());
		c.set(1, 1, 11, Blocks.HAY_BLOCK.defaultBlockState());
		c.set(7, 1, 11, Blocks.HAY_BLOCK.defaultBlockState());
		c.set(2, 1, 2, Blocks.LANTERN.defaultBlockState());
		c.set(6, 1, 2, Blocks.LANTERN.defaultBlockState());
		c.chest(7, 1, 10, BuiltInLootTables.PILLAGER_OUTPOST);

		c.spawnOrc(2, 1, 6, null, null);
		c.spawnOrc(6, 1, 8, null, null);
	}
}
