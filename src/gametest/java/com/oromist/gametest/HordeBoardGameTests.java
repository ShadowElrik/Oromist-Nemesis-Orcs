package com.oromist.gametest;

import com.oromist.horde.HordeBoard;
import com.oromist.horde.HordeBoardLayout;
import com.oromist.horde.game.AugurBoneEvents;
import com.oromist.horde.game.HordeBoardIcons;
import com.oromist.horde.game.HordeBoardMenu;
import com.oromist.nemesis.domain.EncounterContext;
import com.oromist.nemesis.domain.NemesisRepository;
import com.oromist.nemesis.domain.NemesisRules;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcNameGenerator;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.domain.TraitRoller;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/** El tablero de la Horda que abre el hueso del augur. */
@SuppressWarnings("removal") // El jugador simulado de las pruebas vanilla; sigue funcionando en 26.3.
public class HordeBoardGameTests {
	/** Un mundo Némesis de prueba, separado del guardado real. */
	private static NemesisService freshService() {
		Map<UUID, OrcProfile> profiles = new LinkedHashMap<>();
		NemesisRepository repository = new NemesisRepository() {
			@Override
			public Optional<OrcProfile> find(UUID orcId) {
				return Optional.ofNullable(profiles.get(orcId));
			}

			@Override
			public Collection<OrcProfile> all() {
				return profiles.values();
			}

			@Override
			public void save(OrcProfile profile) {
				profiles.put(profile.id(), profile);
			}

			@Override
			public void markChanged() {
			}
		};
		return new NemesisService(repository, NemesisRules.defaults(), new OrcNameGenerator.Syllabic(),
				new TraitRoller.Default(), new SplittableRandom(3));
	}

	/** Un capitán que mató al jugador. */
	private static OrcProfile captainThatKilled(NemesisService service, ServerPlayer player) {
		OrcProfile captain = service.createProfile(OrcArchetype.AXEMAN, 1.0f, 0, OrcRank.CAPTAIN);
		service.onOrcKilledPlayer(captain, new EncounterContext(player.getUUID(), "Steve", 0L, "minecraft:plains"));
		return captain;
	}

	@GameTest
	public void theBoardCannotBeLooted(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		NemesisService service = freshService();
		OrcProfile captain = captainThatKilled(service, player);
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(
				HordeBoard.of(player.getUUID(), List.of(captain), Optional.of(captain.id())));
		int orcSlot = 10; // Primer capitán.
		AtomicBoolean detailsShown = new AtomicBoolean();
		HordeBoardMenu menu = new HordeBoardMenu(1, player.getInventory(), cells, 0L, (p, orc) -> detailsShown.set(true));

		menu.clicked(orcSlot, 0, ContainerInput.PICKUP, player);
		menu.clicked(orcSlot, 0, ContainerInput.QUICK_MOVE, player);

		helper.assertFalse(menu.getSlot(orcSlot).getItem().isEmpty(), "el orco sigue en el tablero");
		helper.assertTrue(menu.getCarried().isEmpty(), "no se puede coger nada");
		helper.assertTrue(player.getInventory().isEmpty(), "nada pasa al inventario");
		helper.assertTrue(detailsShown.get(), "hacer clic sobre un orco muestra su ficha");
		helper.succeed();
	}

	@GameTest
	public void orcIconsShowTheirNameAndHideWeaponStats(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		OrcProfile captain = captainThatKilled(freshService(), player);
		HordeBoard.Entry entry = HordeBoard.of(player.getUUID(), List.of(captain), Optional.empty())
				.section(HordeBoard.Section.CAPTAINS).get(0);

		ItemStack icon = HordeBoardIcons.icon(new HordeBoardLayout.OrcCell(entry), 0L);

		helper.assertTrue(icon.has(DataComponents.CUSTOM_NAME), "el icono lleva el nombre del orco");
		helper.assertFalse(icon.get(DataComponents.TOOLTIP_DISPLAY).shows(DataComponents.ATTRIBUTE_MODIFIERS),
				"no se muestra el daño del arma");
		helper.assertTrue(Boolean.TRUE.equals(icon.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)), "los capitanes brillan");
		helper.succeed();
	}

	@GameTest
	public void theAugurBoneOpensTheBoard(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		NemesisService service = freshService();
		captainThatKilled(service, player);

		AugurBoneEvents.openBoard(player, service);

		helper.assertTrue(player.containerMenu instanceof HordeBoardMenu, "se abre el tablero de la Horda");
		helper.succeed();
	}
}
