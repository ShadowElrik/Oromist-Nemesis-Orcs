package com.oromist.worldgen.buildings;

import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

/** Hoguera central de la aldea, rodeada de bancos y estacas con calaveras. */
public final class BonfireBuilding implements OrcBuilding {
	@Override
	public String id() {
		return "bonfire";
	}

	@Override
	public int width() {
		return 9;
	}

	@Override
	public int depth() {
		return 9;
	}

	@Override
	public int height() {
		return 5;
	}

	@Override
	public void build(BuildContext c) {
		for (int x = 0; x < width(); x++) {
			for (int z = 0; z < depth(); z++) {
				c.foundation(x, z, Palette.COARSE_DIRT);
				c.set(x, 0, z, c.random().nextInt(4) == 0 ? Blocks.GRAVEL.defaultBlockState() : Palette.COARSE_DIRT);
			}
		}
		c.clear(0, 1, 0, 8, 4, 8);

		// Foso de fuego
		c.fill(3, 0, 3, 5, 0, 5, Palette.COBBLE);
		c.set(4, 0, 4, Blocks.NETHERRACK.defaultBlockState());
		c.set(4, 1, 4, Blocks.CAMPFIRE.defaultBlockState());

		// Bancos de troncos
		c.fill(3, 1, 1, 5, 1, 1, Palette.horizontalLog(Blocks.SPRUCE_LOG, Direction.Axis.X));
		c.fill(3, 1, 7, 5, 1, 7, Palette.horizontalLog(Blocks.SPRUCE_LOG, Direction.Axis.X));
		c.fill(1, 1, 3, 1, 1, 5, Palette.horizontalLog(Blocks.SPRUCE_LOG, Direction.Axis.Z));
		c.fill(7, 1, 3, 7, 1, 5, Palette.horizontalLog(Blocks.SPRUCE_LOG, Direction.Axis.Z));

		// Estacas con calaveras en las esquinas
		int[][] corners = {{0, 0}, {8, 0}, {0, 8}, {8, 8}};
		for (int[] corner : corners) {
			c.fill(corner[0], 1, corner[1], corner[0], 2, corner[1], Palette.DARK_FENCE);
			c.set(corner[0], 3, corner[1], Palette.SKULL);
		}

		c.spawnOrc(2, 1, 4, null, null);
		c.spawnOrc(6, 1, 4, null, null);
	}
}
