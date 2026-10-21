package com.oromist.registry;

import com.oromist.OromistNPC;
import com.oromist.worldgen.OrcBuildingPiece;
import com.oromist.worldgen.OrcVillageStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Registro del tipo de estructura y de pieza de la aldea orca. La estructura concreta,
 * sus biomas y su rareza se definen como datos en {@code data/oromist-npc/worldgen}.
 */
public final class ModStructures {
	public static StructureType<OrcVillageStructure> ORC_VILLAGE;
	public static StructurePieceType ORC_BUILDING_PIECE;

	private ModStructures() {
	}

	public static void register() {
		ORC_VILLAGE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, OromistNPC.id("orc_village"),
				() -> OrcVillageStructure.CODEC);
		ORC_BUILDING_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, OromistNPC.id("orc_building"),
				(StructurePieceType.ContextlessType) OrcBuildingPiece::new);
	}
}
