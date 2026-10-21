package com.oromist.worldgen.buildings;

import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/** Fragua abierta: yunque, alto horno y un caldero de lava. Aquí se forjan las hachas orcas. */
public final class ForgeBuilding implements OrcBuilding {
	private static final int SIZE = 7;

	@Override
	public String id() {
		return "forge";
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
		return 5;
	}

	@Override
	public void build(BuildContext c) {
		int max = SIZE - 1;
		for (int x = 0; x <= max; x++) {
			for (int z = 0; z <= max; z++) {
				c.foundation(x, z, Palette.COBBLE);
				c.set(x, 0, z, Blocks.STONE_BRICKS.defaultBlockState());
			}
		}
		c.clear(0, 1, 0, max, 4, max);

		for (int[] corner : new int[][] {{0, 0}, {max, 0}, {0, max}, {max, max}}) {
			c.fill(corner[0], 1, corner[1], corner[0], 3, corner[1], Palette.LOG);
		}
		c.fill(1, 1, max, max - 1, 3, max, Palette.COBBLE);
		c.fill(0, 4, 0, max, 4, max, Blocks.SPRUCE_SLAB.defaultBlockState());

		c.set(1, 1, 4, Blocks.ANVIL.defaultBlockState());
		c.set(3, 1, 5, Blocks.BLAST_FURNACE.defaultBlockState());
		c.set(5, 1, 5, Blocks.SMITHING_TABLE.defaultBlockState());
		c.set(3, 1, 3, Blocks.LAVA_CAULDRON.defaultBlockState());
		c.chest(5, 1, 3, BuiltInLootTables.VILLAGE_WEAPONSMITH);

		c.spawnOrc(2, 1, 2, null, OrcArchetype.AXEMAN);
	}
}
