package com.oromist.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/**
 * Distribuye los edificios de una aldea orca alrededor de la hoguera central,
 * con las fachadas mirando al centro, sin solaparse y evitando agua y pendientes fuertes.
 */
public final class VillageLayoutPlanner {
	private static final int ATTEMPTS_PER_BUILDING = 16;
	private static final int SPACING = 2;
	private static final int MAX_SLOPE = 3;

	private final Structure.GenerationContext context;
	private final WorldgenRandom random;
	private final List<BoundingBox> occupied = new ArrayList<>();

	public VillageLayoutPlanner(Structure.GenerationContext context) {
		this.context = context;
		this.random = context.random();
	}

	public void plan(BlockPos center, StructurePiecesBuilder builder) {
		placeAt(OrcBuildings.BONFIRE, center.getX(), center.getZ(), Direction.SOUTH).ifPresent(builder::addPiece);
		placeAround(OrcBuildings.CHIEFTAIN_TENT, center, 12, 16).ifPresent(builder::addPiece);

		List<OrcBuilding> others = new ArrayList<>();
		addCopies(others, OrcBuildings.HUT, 4 + random.nextInt(4));
		addCopies(others, OrcBuildings.WATCHTOWER, 2);
		addCopies(others, OrcBuildings.FORGE, 1);
		addCopies(others, OrcBuildings.LONGHOUSE, random.nextInt(2));
		addCopies(others, OrcBuildings.TOTEM, 2 + random.nextInt(3));
		addCopies(others, OrcBuildings.FARM_PLOT, 1 + random.nextInt(2));
		Collections.shuffle(others, new java.util.Random(random.nextLong()));

		for (OrcBuilding building : others) {
			placeAround(building, center, 10, 34).ifPresent(builder::addPiece);
		}
	}

	private static void addCopies(List<OrcBuilding> into, OrcBuilding building, int amount) {
		for (int i = 0; i < amount; i++) {
			into.add(building);
		}
	}

	/** Prueba varias posiciones en un anillo alrededor del centro. */
	private Optional<OrcBuildingPiece> placeAround(OrcBuilding building, BlockPos center, int minRadius, int maxRadius) {
		for (int attempt = 0; attempt < ATTEMPTS_PER_BUILDING; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
			int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
			Direction facing = directionTowards(x, z, center.getX(), center.getZ());
			Optional<OrcBuildingPiece> piece = placeAt(building, x, z, facing);
			if (piece.isPresent()) {
				return piece;
			}
		}
		return Optional.empty();
	}

	/** Intenta colocar el edificio centrado en (x, z). */
	private Optional<OrcBuildingPiece> placeAt(OrcBuilding building, int centerX, int centerZ, Direction facing) {
		boolean alongZ = facing.getAxis() == Direction.Axis.Z;
		int sizeX = alongZ ? building.width() : building.depth();
		int sizeZ = alongZ ? building.depth() : building.width();
		int minX = centerX - sizeX / 2;
		int minZ = centerZ - sizeZ / 2;

		BoundingBox footprint = OrcBuildingPiece.footprint(building, minX, 0, minZ, facing);
		BoundingBox padded = footprint.inflatedBy(SPACING);
		for (BoundingBox other : occupied) {
			if (horizontallyIntersects(padded, other)) {
				return Optional.empty();
			}
		}

		int meanY = Structure.getMeanFirstOccupiedHeight(context, minX, sizeX - 1, minZ, sizeZ - 1);
		int lowestY = lowestCorner(minX, minZ, sizeX - 1, sizeZ - 1);
		if (meanY - lowestY > MAX_SLOPE || isUnderwater(centerX, centerZ)) {
			return Optional.empty();
		}

		occupied.add(footprint);
		// El suelo (local y = 0) sustituye al bloque de superficie.
		return Optional.of(new OrcBuildingPiece(building, minX, meanY - 1, minZ, facing));
	}

	private int lowestCorner(int minX, int minZ, int spanX, int spanZ) {
		int lowest = Integer.MAX_VALUE;
		for (int[] corner : new int[][] {{minX, minZ}, {minX + spanX, minZ}, {minX, minZ + spanZ}, {minX + spanX, minZ + spanZ}}) {
			lowest = Math.min(lowest, surfaceHeight(corner[0], corner[1], Heightmap.Types.WORLD_SURFACE_WG));
		}
		return lowest;
	}

	private boolean isUnderwater(int x, int z) {
		return surfaceHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG) < surfaceHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG);
	}

	private int surfaceHeight(int x, int z, Heightmap.Types type) {
		return context.chunkGenerator().getFirstOccupiedHeight(x, z, type, context.heightAccessor(), context.randomState());
	}

	private static boolean horizontallyIntersects(BoundingBox a, BoundingBox b) {
		return a.maxX() >= b.minX() && a.minX() <= b.maxX() && a.maxZ() >= b.minZ() && a.minZ() <= b.maxZ();
	}

	/** Dirección cardinal desde (x, z) hacia el objetivo. */
	private static Direction directionTowards(int x, int z, int targetX, int targetZ) {
		int dx = targetX - x;
		int dz = targetZ - z;
		if (Math.abs(dx) > Math.abs(dz)) {
			return dx > 0 ? Direction.EAST : Direction.WEST;
		}
		return dz > 0 ? Direction.SOUTH : Direction.NORTH;
	}
}
