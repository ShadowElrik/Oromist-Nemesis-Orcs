package com.oromist.worldgen.buildings;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Materiales y utilidades compartidas por los edificios orcos. */
final class Palette {
	static final BlockState AIR = Blocks.AIR.defaultBlockState();
	static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
	static final BlockState COBBLE = Blocks.COBBLESTONE.defaultBlockState();
	static final BlockState MOSSY_COBBLE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
	static final BlockState LOG = Blocks.SPRUCE_LOG.defaultBlockState();
	static final BlockState DARK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
	static final BlockState PLANKS = Blocks.SPRUCE_PLANKS.defaultBlockState();
	static final BlockState DARK_PLANKS = Blocks.DARK_OAK_PLANKS.defaultBlockState();
	static final BlockState FENCE = Blocks.SPRUCE_FENCE.defaultBlockState();
	static final BlockState DARK_FENCE = Blocks.DARK_OAK_FENCE.defaultBlockState();
	static final BlockState SKULL = Blocks.SKELETON_SKULL.defaultBlockState();
	static final BlockState BONES = Blocks.BONE_BLOCK.defaultBlockState();
	// En 26.x los bloques de color se agrupan en ColorCollection (en versiones previas: Blocks.RED_WOOL).
	static final BlockState RED_WOOL = Blocks.WOOL.red().defaultBlockState();
	static final BlockState BLACK_WOOL = Blocks.WOOL.black().defaultBlockState();

	private Palette() {
	}

	static BlockState horizontalLog(Block log, Direction.Axis axis) {
		return log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	static BlockState facing(Block block, Direction direction) {
		return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
	}

	/** Pared "sucia" orca: mezcla de barro, adobe y piedra. */
	static BlockState grimyWall(RandomSource random) {
		int roll = random.nextInt(10);
		if (roll < 5) {
			return Blocks.MUD_BRICKS.defaultBlockState();
		} else if (roll < 8) {
			return Blocks.PACKED_MUD.defaultBlockState();
		}
		return COBBLE;
	}

	static BlockState stoneFloor(RandomSource random) {
		return random.nextInt(3) == 0 ? MOSSY_COBBLE : COBBLE;
	}
}
