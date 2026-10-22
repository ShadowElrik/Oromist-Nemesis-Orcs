package com.oromist.horde.game;

import com.oromist.entity.OrcDisplayNames;
import com.oromist.horde.HordeBoard;
import com.oromist.horde.HordeBoardLayout;
import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.game.NemesisRuntime;
import com.oromist.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * El hueso del augur: los chamanes "leen los huesos" para conocer a la Horda. Al usarlo se
 * abre el tablero con los orcos que conoces, por rango, con sus rasgos descubiertos y recuerdos.
 * No se gasta.
 */
public final class AugurBoneEvents {
	/** Medio segundo entre lecturas, para que no se abra dos veces con un doble clic. */
	private static final int COOLDOWN_TICKS = 10;
	/** Recuerdos que se escriben en el chat al hacer clic sobre un orco. */
	private static final int MAX_MEMORIES_IN_CHAT = 10;

	private final NemesisRuntime runtime;

	public AugurBoneEvents(NemesisRuntime runtime) {
		this.runtime = runtime;
	}

	public void register() {
		UseItemCallback.EVENT.register(this::onUseItem);
	}

	private InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(ModItems.AUGUR_BONE)) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			runtime.service().ifPresent(service -> openBoard(serverPlayer, service));
			player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		}
		return InteractionResult.SUCCESS;
	}

	public static void openBoard(ServerPlayer player, NemesisService service) {
		HordeBoard board = HordeBoard.of(player.getUUID(), service.knownTo(player.getUUID()),
				service.nemesisOf(player.getUUID(), profile -> true).map(OrcProfile::id));
		List<HordeBoardLayout.Cell> cells = HordeBoardLayout.place(board);
		long now = player.level().getGameTime();
		player.openMenu(new SimpleMenuProvider(
				(containerId, inventory, opener) -> new HordeBoardMenu(containerId, inventory, cells, now,
						(clicker, orc) -> showDetails(clicker, orc.entry(), now)),
				Component.translatable("horde.oromist-npc.title")));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SKELETON_AMBIENT,
				SoundSource.PLAYERS, 0.6F, 0.6F);
	}

	/** Ficha completa en el chat: lo que no cabe en la descripción del menú. */
	private static void showDetails(Player player, HordeBoard.Entry entry, long now) {
		OrcProfile profile = entry.profile();
		player.sendSystemMessage(Component.literal("— ").withStyle(ChatFormatting.DARK_GRAY)
				.append(OrcDisplayNames.fullName(profile))
				.append(Component.literal(" —").withStyle(ChatFormatting.DARK_GRAY)));
		HordeBoardIcons.summary(entry).forEach(player::sendSystemMessage);
		List<Memory> memories = profile.memoriesAbout(player.getUUID()).stream()
				.sorted((a, b) -> Long.compare(b.gameTime(), a.gameTime()))
				.limit(MAX_MEMORIES_IN_CHAT)
				.toList();
		for (Memory memory : memories) {
			player.sendSystemMessage(Component.literal("  - ").append(HordeBoardIcons.memoryText(memory, now))
					.withStyle(ChatFormatting.GRAY));
		}
	}
}
