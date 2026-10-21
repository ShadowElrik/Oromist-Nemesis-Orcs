package com.oromist.ambush;

import com.oromist.registry.ModEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Busca dónde hacer aparecer a los emboscadores. Hay una estrategia para superficie
 * (casa y viajes) y otra subterránea (minería).
 */
public interface SpawnLocator {
	int ATTEMPTS = 24;

	Optional<BlockPos> find(ServerLevel level, ServerPlayer player, RandomSource random);

	static SpawnLocator forActivity(PlayerActivity activity) {
		return switch (activity) {
			case MINING -> new Underground(8, 16);
			case AT_HOME -> new Surface(18, 30);
			case TRAVELING -> new Surface(14, 24);
		};
	}

	/**
	 * Comprueba suelo natural firme, espacio libre (para orcos altos) y que no haya líquidos.
	 * Exigir terreno natural evita que aparezcan sobre tejados o dentro de construcciones.
	 */
	static boolean isValidSpot(ServerLevel level, BlockPos pos) {
		if (!level.isLoaded(pos) || !level.getFluidState(pos).isEmpty()) {
			return false;
		}
		BlockPos below = pos.below();
		BlockState ground = level.getBlockState(below);
		if (!isNaturalGround(ground) || !ground.isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}
		AABB box = ModEntities.ORC.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(0.2, 0.0, 0.2)
				.expandTowards(0.0, 0.7, 0.0);
		return level.noCollision(box);
	}

	static boolean isNaturalGround(BlockState state) {
		return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD)
				|| state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.SNOW) || state.is(Blocks.GRAVEL);
	}

	/** Punto aleatorio en un anillo alrededor del jugador, sobre la superficie. */
	record Surface(int minDistance, int maxDistance) implements SpawnLocator {
		@Override
		public Optional<BlockPos> find(ServerLevel level, ServerPlayer player, RandomSource random) {
			for (int i = 0; i < ATTEMPTS; i++) {
				double angle = random.nextDouble() * Math.PI * 2;
				int distance = minDistance + random.nextInt(maxDistance - minDistance + 1);
				int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
				int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
				BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
				if (isValidSpot(level, pos)) {
					return Optional.of(pos);
				}
			}
			return Optional.empty();
		}
	}

	/** Busca huecos de cueva/túnel a una altura parecida a la del jugador. */
	record Underground(int minDistance, int maxDistance) implements SpawnLocator {
		private static final int VERTICAL_RANGE = 6;

		@Override
		public Optional<BlockPos> find(ServerLevel level, ServerPlayer player, RandomSource random) {
			BlockPos origin = player.blockPosition();
			for (int i = 0; i < ATTEMPTS * 2; i++) {
				double angle = random.nextDouble() * Math.PI * 2;
				int distance = minDistance + random.nextInt(maxDistance - minDistance + 1);
				int x = (int) Math.floor(origin.getX() + Math.cos(angle) * distance);
				int z = (int) Math.floor(origin.getZ() + Math.sin(angle) * distance);
				for (int dy = -VERTICAL_RANGE; dy <= VERTICAL_RANGE; dy++) {
					BlockPos pos = new BlockPos(x, origin.getY() + dy, z);
					if (!level.canSeeSky(pos) && isValidSpot(level, pos)) {
						return Optional.of(pos);
					}
				}
			}
			return Optional.empty();
		}
	}
}
