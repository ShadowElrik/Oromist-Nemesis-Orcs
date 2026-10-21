package com.oromist.worldgen;

import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.registry.ModEntities;
import com.oromist.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

/**
 * Pieza de estructura genérica que construye cualquier {@link OrcBuilding}.
 * Se encarga de lo propio de Minecraft (rotación, recorte por chunk, guardado),
 * de modo que los edificios solo describen su forma.
 */
public final class OrcBuildingPiece extends StructurePiece {
	private static final String BUILDING_TAG = "Building";

	private final OrcBuilding building;

	/**
	 * @param minX   esquina mínima X en el mundo
	 * @param floorY altura del suelo del edificio (local y = 0)
	 * @param minZ   esquina mínima Z en el mundo
	 * @param facing hacia dónde mira la fachada (local z = 0)
	 */
	public OrcBuildingPiece(OrcBuilding building, int minX, int floorY, int minZ, Direction facing) {
		super(ModStructures.ORC_BUILDING_PIECE, 0, makeBoundingBox(minX, floorY, minZ, facing.getOpposite(),
				building.width(), building.height(), building.depth()));
		this.building = building;
		setOrientation(facing.getOpposite());
	}

	public OrcBuildingPiece(CompoundTag tag) {
		super(ModStructures.ORC_BUILDING_PIECE, tag);
		String id = tag.getStringOr(BUILDING_TAG, OrcBuildings.HUT.id());
		this.building = OrcBuildings.byId(id).orElse(OrcBuildings.HUT);
	}

	/** Caja que ocuparía el edificio, para que el planificador evite solapes antes de crearlo. */
	public static BoundingBox footprint(OrcBuilding building, int minX, int floorY, int minZ, Direction facing) {
		return makeBoundingBox(minX, floorY, minZ, facing.getOpposite(), building.width(), building.height(), building.depth());
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putString(BUILDING_TAG, building.id());
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
		building.build(new PieceContext(level, chunkBB, random));
	}

	/** Implementación de {@link BuildContext} sobre los métodos protegidos de {@link StructurePiece}. */
	private final class PieceContext implements BuildContext {
		private final WorldGenLevel level;
		private final BoundingBox chunkBB;
		private final RandomSource random;

		private PieceContext(WorldGenLevel level, BoundingBox chunkBB, RandomSource random) {
			this.level = level;
			this.chunkBB = chunkBB;
			this.random = random;
		}

		@Override
		public void set(int x, int y, int z, BlockState state) {
			placeBlock(level, state, x, y, z, chunkBB);
		}

		@Override
		public void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
			generateBox(level, chunkBB, x1, y1, z1, x2, y2, z2, state, state, false);
		}

		@Override
		public void clear(int x1, int y1, int z1, int x2, int y2, int z2) {
			generateAirBox(level, chunkBB, x1, y1, z1, x2, y2, z2);
		}

		@Override
		public void foundation(int x, int z, BlockState state) {
			fillColumnDown(level, state, x, -1, z, chunkBB);
		}

		@Override
		public void chest(int x, int y, int z, ResourceKey<LootTable> lootTable) {
			createChest(level, chunkBB, random, x, y, z, lootTable);
		}

		@Override
		public void spawnOrc(int x, int y, int z, @Nullable OrcRank leaderRank, @Nullable OrcArchetype archetype) {
			BlockPos pos = getWorldPos(x, y, z);
			if (!chunkBB.isInside(pos)) {
				return; // Otro chunk de esta misma pieza se encargará de él.
			}
			OrcEntity orc = ModEntities.ORC.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
			if (orc == null) {
				return;
			}
			orc.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
			orc.setPendingRank(leaderRank);
			orc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
			if (archetype != null) {
				orc.prepareAs(archetype, random);
			}
			orc.setPersistenceRequired();
			level.addFreshEntityWithPassengers(orc);
		}

		@Override
		public RandomSource random() {
			return random;
		}
	}
}
