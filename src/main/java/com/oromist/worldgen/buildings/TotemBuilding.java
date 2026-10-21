package com.oromist.worldgen.buildings;

import com.oromist.worldgen.BuildContext;
import com.oromist.worldgen.OrcBuilding;
import net.minecraft.world.level.block.Blocks;

/** Tótem de guerra: un poste con calavera, huesos y estandartes de lana roja. */
public final class TotemBuilding implements OrcBuilding {
	@Override
	public String id() {
		return "totem";
	}

	@Override
	public int width() {
		return 3;
	}

	@Override
	public int depth() {
		return 3;
	}

	@Override
	public int height() {
		return 7;
	}

	@Override
	public void build(BuildContext c) {
		c.foundation(1, 1, Palette.COBBLE);
		c.set(1, 0, 1, Palette.BONES);
		c.clear(0, 1, 0, 2, 6, 2);
		c.fill(1, 1, 1, 1, 4, 1, Palette.DARK_LOG);
		c.set(0, 3, 1, Palette.DARK_FENCE);
		c.set(2, 3, 1, Palette.DARK_FENCE);
		c.set(0, 4, 1, Palette.RED_WOOL);
		c.set(2, 4, 1, Palette.RED_WOOL);
		c.set(1, 5, 1, Palette.SKULL);
	}
}
