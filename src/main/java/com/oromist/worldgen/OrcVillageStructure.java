package com.oromist.worldgen;

import com.mojang.serialization.MapCodec;
import com.oromist.registry.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Aldea orca: hoguera central, tienda del jefe, chozas, barracones, fragua, torres de
 * vigía y tótems. La rareza la define el {@code structure_set} en los datos del mod
 * (por defecto igual que las aldeas vanilla: spacing 34, separation 8).
 */
public final class OrcVillageStructure extends Structure {
	public static final MapCodec<OrcVillageStructure> CODEC = simpleCodec(OrcVillageStructure::new);

	public OrcVillageStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunk = context.chunkPos();
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		int surface = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
				context.heightAccessor(), context.randomState());
		int floor = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
				context.heightAccessor(), context.randomState());
		if (floor < surface) {
			return Optional.empty(); // El centro cae en agua.
		}
		BlockPos center = new BlockPos(x, surface, z);
		return Optional.of(new GenerationStub(center, builder -> new VillageLayoutPlanner(context).plan(center, builder)));
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.ORC_VILLAGE;
	}
}
