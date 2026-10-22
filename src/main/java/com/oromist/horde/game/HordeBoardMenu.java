package com.oromist.horde.game;

import com.oromist.horde.HordeBoardLayout;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Menú de solo lectura con el tablero de la Horda. Usa el cofre doble vanilla, así que
 * <b>no necesita nada en el cliente</b>: funciona igual en un servidor.
 *
 * <p>Ningún clic mueve objetos: el clic sobre un orco solo avisa a {@code onOrcClicked}
 * (que muestra su ficha completa en el chat).</p>
 */
public class HordeBoardMenu extends ChestMenu {
	private final List<HordeBoardLayout.Cell> cells;
	private final BiConsumer<Player, HordeBoardLayout.OrcCell> onOrcClicked;

	public HordeBoardMenu(int containerId, Inventory inventory, List<HordeBoardLayout.Cell> cells, long now,
			BiConsumer<Player, HordeBoardLayout.OrcCell> onOrcClicked) {
		super(MenuType.GENERIC_9x6, containerId, inventory, filledContainer(cells, now), HordeBoardLayout.ROWS);
		this.cells = cells;
		this.onOrcClicked = onOrcClicked;
	}

	private static SimpleContainer filledContainer(List<HordeBoardLayout.Cell> cells, long now) {
		SimpleContainer container = new SimpleContainer(HordeBoardLayout.SIZE);
		for (int slot = 0; slot < HordeBoardLayout.SIZE; slot++) {
			container.setItem(slot, HordeBoardIcons.icon(cells.get(slot), now));
		}
		return container;
	}

	@Override
	public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
		// No se llama a super: así nada se puede sacar, poner ni intercambiar.
		if (slotIndex >= 0 && slotIndex < cells.size() && cells.get(slotIndex) instanceof HordeBoardLayout.OrcCell orc) {
			onOrcClicked.accept(player, orc);
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return player.isAlive();
	}
}
