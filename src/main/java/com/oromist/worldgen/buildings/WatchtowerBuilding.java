package com.oromist.worldgen.buildings;

import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;

/** Torre de vigía de troncos con un tirador en lo alto. */
public final class WatchtowerBuilding implements OrcBuilding {
	private static final int SIZE = 5;
	private static final int PLATFORM_Y = 8;

	@Override
	public String id() {
		return "watchtower";
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
		return 12;
	}

	@Override
	public void build(BuildContext c) {
		int max = SIZE - 1;
		for (int x = 0; x <= max; x++) {
			for (int z = 0; z <= max; z++) {
				c.foundation(x, z, Palette.COBBLE);
				c.set(x, 0, z, Palette.stoneFloor(c.random()));
			}
		}
		c.clear(0, 1, 0, max, 11, max);

		for (int[] corner : new int[][] {{0, 0}, {max, 0}, {0, max}, {max, max}}) {
			c.fill(corner[0], 1, corner[1], corner[0], PLATFORM_Y + 2, corner[1], Palette.LOG);
		}

		// Escalera de mano apoyada en una columna de tablones
		c.fill(2, 1, 3, 2, PLATFORM_Y, 3, Palette.PLANKS);
		c.fill(2, 1, 2, 2, PLATFORM_Y, 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));

		// Plataforma con hueco para la escalera y barandilla
		c.fill(0, PLATFORM_Y, 0, max, PLATFORM_Y, max, Palette.PLANKS);
		c.set(2, PLATFORM_Y, 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
		for (int i = 1; i < max; i++) {
			c.set(i, PLATFORM_Y + 1, 0, Palette.FENCE);
			c.set(i, PLATFORM_Y + 1, max, Palette.FENCE);
			c.set(0, PLATFORM_Y + 1, i, Palette.FENCE);
			c.set(max, PLATFORM_Y + 1, i, Palette.FENCE);
		}
		c.fill(0, PLATFORM_Y + 3, 0, max, PLATFORM_Y + 3, max, Blocks.DARK_OAK_SLAB.defaultBlockState());
		c.set(2, PLATFORM_Y + 4, 2, Palette.SKULL);

		OrcArchetype shooter = c.random().nextBoolean() ? OrcArchetype.ARCHER : OrcArchetype.CROSSBOWMAN;
		c.spawnOrc(1, PLATFORM_Y + 1, 1, null, shooter);
	}
}
