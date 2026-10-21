package com.oromist.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oromist.ambush.AmbushDirector;
import com.oromist.ambush.PlayerActivity;
import com.oromist.entity.OrcDisplayNames;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcTrait;
import com.oromist.nemesis.game.NemesisRuntime;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando {@code /nemesis}:
 * <ul>
 *   <li>{@code /nemesis} — lista los orcos que te odian (el primero es tu némesis).</li>
 *   <li>{@code /nemesis player <jugador>} — (operadores) lo mismo para otro jugador.</li>
 *   <li>{@code /nemesis ambush <home|mining|travel>} — (operadores) fuerza una emboscada para probar.</li>
 * </ul>
 */
public final class NemesisCommand {
	/** Máximo de orcos mostrados, para no inundar el chat en partidas largas. */
	private static final int MAX_LISTED = 10;

	private final NemesisRuntime runtime;
	private final AmbushDirector ambushDirector;

	public NemesisCommand(NemesisRuntime runtime, AmbushDirector ambushDirector) {
		this.runtime = runtime;
		this.ambushDirector = ambushDirector;
	}

	public void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> build(dispatcher));
	}

	private void build(CommandDispatcher<CommandSourceStack> dispatcher) {
		var ambush = Commands.literal("ambush").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
		for (PlayerActivity activity : PlayerActivity.values()) {
			ambush.then(Commands.literal(activityName(activity)).executes(ctx -> forceAmbush(ctx, activity)));
		}
		// /nemesis player <jugador>: para que los administradores consulten a cualquier jugador.
		var ofPlayer = Commands.literal("player").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("target", EntityArgument.player())
						.executes(ctx -> list(ctx, EntityArgument.getPlayer(ctx, "target"))));
		dispatcher.register(Commands.literal("nemesis")
				.executes(ctx -> list(ctx, ctx.getSource().getPlayerOrException()))
				.then(ofPlayer)
				.then(ambush));
	}

	private int list(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
		NemesisService service = runtime.service().orElse(null);
		if (service == null) {
			return 0;
		}
		List<OrcProfile> enemies = service.enemiesOf(player.getUUID());
		if (enemies.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.translatable("command.oromist-npc.no_enemies"), false);
			return 0;
		}
		ctx.getSource().sendSuccess(() -> Component.translatable("command.oromist-npc.header").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		int shown = Math.min(enemies.size(), MAX_LISTED);
		for (int i = 0; i < shown; i++) {
			OrcProfile profile = enemies.get(i);
			boolean isNemesis = i == 0;
			ctx.getSource().sendSuccess(() -> describe(profile, player, isNemesis), false);
		}
		int hidden = enemies.size() - shown;
		if (hidden > 0) {
			ctx.getSource().sendSuccess(() -> Component.translatable("command.oromist-npc.more", hidden).withStyle(ChatFormatting.GRAY), false);
		}
		return enemies.size();
	}

	private static Component describe(OrcProfile profile, ServerPlayer player, boolean isNemesis) {
		MutableComponent line = Component.literal(isNemesis ? "☠ " : "• ").withStyle(ChatFormatting.DARK_RED)
				.append(OrcDisplayNames.fullName(profile))
				.append(Component.literal(" — ").withStyle(ChatFormatting.DARK_GRAY))
				.append(OrcDisplayNames.rankName(profile.rank()))
				.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.translatable(profile.archetype().translationKey()));
		if (!profile.traits().isEmpty()) {
			line.append(Component.literal(" ["));
			boolean first = true;
			for (OrcTrait trait : profile.traits()) {
				if (!first) {
					line.append(Component.literal(", "));
				}
				ChatFormatting color = trait.kind() == OrcTrait.TraitKind.STRENGTH ? ChatFormatting.GOLD : ChatFormatting.AQUA;
				line.append(Component.translatable(trait.translationKey()).withStyle(color));
				first = false;
			}
			line.append(Component.literal("]"));
		}
		line.append(Component.translatable("command.oromist-npc.grudge", profile.grudgeAgainst(player.getUUID()))
				.withStyle(ChatFormatting.GRAY));
		return line;
	}

	private int forceAmbush(CommandContext<CommandSourceStack> ctx, PlayerActivity activity) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		boolean spawned = runtime.service().map(service -> ambushDirector.trigger(player, activity, service)).orElse(false);
		if (!spawned) {
			ctx.getSource().sendFailure(Component.translatable("command.oromist-npc.ambush_failed"));
		}
		return spawned ? 1 : 0;
	}

	private static String activityName(PlayerActivity activity) {
		return switch (activity) {
			case AT_HOME -> "home";
			case MINING -> "mining";
			case TRAVELING -> "travel";
		};
	}
}
