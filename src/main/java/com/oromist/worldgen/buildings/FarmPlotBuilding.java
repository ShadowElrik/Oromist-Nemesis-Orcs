package com.oromist.worldgen.buildings;

import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Huerto orco: papas y betarragas regadas por un canal. Los orcos cultivan poco y solo
 * para acompañar la carne, que se ahúma al fondo del huerto.
 */
public final class FarmPlotBuilding implements OrcBuilding {
	private static final int WIDTH = 9;
	private static final int DEPTH = 8;
	private static final int CHANNEL_X = 4;

	@Override
	public String id() {
		return "farm_plot";
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
		return 4;
	}

	@Override
	public void build(BuildContext c) {
		int maxX = WIDTH - 1;
		int maxZ = DEPTH - 1;
		for (int x = 0; x <= maxX; x++) {
			for (int z = 0; z <= maxZ; z++) {
				c.foundation(x, z, Palette.COARSE_DIRT);
				c.set(x, 0, z, Palette.COARSE_DIRT);
			}
		}
		c.clear(0, 1, 0, maxX, 3, maxZ);

		// Cerco con entrada al frente
		for (int x = 0; x <= maxX; x++) {
			c.set(x, 1, 0, Palette.FENCE);
			c.set(x, 1, maxZ, Palette.FENCE);
		}
		for (int z = 0; z <= maxZ; z++) {
			c.set(0, 1, z, Palette.FENCE);
			c.set(maxX, 1, z, Palette.FENCE);
		}
		c.set(CHANNEL_X, 1, 0, Palette.AIR);

		// Canal de riego y surcos
		for (int z = 1; z <= 5; z++) {
			c.set(CHANNEL_X, 0, z, Blocks.WATER.defaultBlockState());
			for (int x = 1; x < maxX; x++) {
				if (x == CHANNEL_X) {
					continue;
				}
				c.set(x, 0, z, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
				c.set(x, 1, z, crop(c, x < CHANNEL_X));
			}
		}

		// Fondo: ahumador para la carne, compostera y paja
		c.set(1, 1, 6, Palette.facing(Blocks.SMOKER, Direction.SOUTH));
		c.set(2, 1, 6, Blocks.BARREL.defaultBlockState());
		c.set(6, 1, 6, Blocks.COMPOSTER.defaultBlockState());
		c.set(7, 1, 6, Blocks.HAY_BLOCK.defaultBlockState());
		c.set(1, 2, 6, Palette.SKULL);

		c.spawnOrc(CHANNEL_X, 1, 6, null, null);
	}

	private static BlockState crop(BuildContext c, boolean potatoes) {
		return potatoes
				? Blocks.POTATOES.defaultBlockState().setValue(BlockStateProperties.AGE_7, c.random().nextInt(8))
				: Blocks.BEETROOTS.defaultBlockState().setValue(BlockStateProperties.AGE_3, c.random().nextInt(4));
	}
}
