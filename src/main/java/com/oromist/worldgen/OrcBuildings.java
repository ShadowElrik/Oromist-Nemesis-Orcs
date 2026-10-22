package com.oromist.worldgen;

import com.oromist.worldgen.buildings.BonfireBuilding;
import com.oromist.worldgen.buildings.ChieftainTentBuilding;
import com.oromist.worldgen.buildings.FarmPlotBuilding;
import com.oromist.worldgen.buildings.ForgeBuilding;
import com.oromist.worldgen.buildings.HutBuilding;
import com.oromist.worldgen.buildings.LonghouseBuilding;
import com.oromist.worldgen.buildings.TotemBuilding;
import com.oromist.worldgen.buildings.WatchtowerBuilding;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Catálogo de edificios disponibles, por id. Añadir un edificio = registrarlo aquí. */
public final class OrcBuildings {
	public static final OrcBuilding BONFIRE = new BonfireBuilding();
	public static final OrcBuilding CHIEFTAIN_TENT = new ChieftainTentBuilding();
	public static final OrcBuilding HUT = new HutBuilding();
	public static final OrcBuilding LONGHOUSE = new LonghouseBuilding();
	public static final OrcBuilding WATCHTOWER = new WatchtowerBuilding();
	public static final OrcBuilding FORGE = new ForgeBuilding();
	public static final OrcBuilding TOTEM = new TotemBuilding();
	public static final OrcBuilding FARM_PLOT = new FarmPlotBuilding();

	private static final Map<String, OrcBuilding> BY_ID = new LinkedHashMap<>();

	static {
		for (OrcBuilding building : new OrcBuilding[] {BONFIRE, CHIEFTAIN_TENT, HUT, LONGHOUSE, WATCHTOWER, FORGE, TOTEM, FARM_PLOT}) {
			BY_ID.put(building.id(), building);
		}
	}

	private OrcBuildings() {
	}

	public static Optional<OrcBuilding> byId(String id) {
		return Optional.ofNullable(BY_ID.get(id));
	}
}
