package com.oromist.dialogue.game;

import com.oromist.dialogue.DialogueLine;
import com.oromist.dialogue.DialogueSelector;
import com.oromist.dialogue.DialogueTrigger;
import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.game.NemesisRuntime;
import com.oromist.nemesis.game.OrcProfiles;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Muestra los diálogos en el chat con el formato {@code [Gorbag el Cruel] «frase»} y
 * reproduce un gruñido. Aplica un tiempo de espera por pareja orco/jugador para que
 * los orcos no repitan frases constantemente.
 */
public final class ChatDialogueSpeaker implements DialogueSpeaker {
	private final NemesisRuntime runtime;
	private final DialogueSelector selector;
	private final SplittableRandom random = new SplittableRandom();
	/** Última frase de cada pareja orco/jugador. */
	private final Map<String, Long> lastSpoken = new HashMap<>();
	/** Última frase de cada orco, a quien sea: en peleas de grupo evita que hable con todos a la vez. */
	private final Map<UUID, Long> lastSpokenByOrc = new HashMap<>();
	/** Silencio mínimo de un mismo orco entre dos saludos (ticks). */
	private static final long ORC_COOLDOWN_TICKS = 15 * 20;
	/** Tamaño a partir del cual se limpian entradas viejas de los mapas de enfriamiento. */
	private static final int CLEANUP_THRESHOLD = 512;

	public ChatDialogueSpeaker(NemesisRuntime runtime, DialogueSelector selector) {
		this.runtime = runtime;
		this.selector = selector;
	}

	@Override
	public void speak(OrcEntity speaker, ServerPlayer target, DialogueTrigger trigger) {
		runtime.service().flatMap(service -> OrcProfiles.of(speaker, service)).ifPresent(profile -> {
			ServerLevel level = (ServerLevel) speaker.level();
			long now = level.getGameTime();
			if (trigger == DialogueTrigger.SIGHTED && isOnCooldown(profile.id(), target.getUUID(), now)) {
				return;
			}
			selector.select(profile, target.getUUID(), target.getPlainTextName(), trigger, now, random)
					.ifPresent(line -> broadcast(level, speaker, profile, line));
			lastSpoken.put(key(profile.id(), target.getUUID()), now);
			lastSpokenByOrc.put(profile.id(), now);
			cleanUp(now);
		});
	}

	private boolean isOnCooldown(UUID orcId, UUID playerId, long now) {
		Long lastToPlayer = lastSpoken.get(key(orcId, playerId));
		Long lastByOrc = lastSpokenByOrc.get(orcId);
		return within(lastToPlayer, now, pairCooldownTicks()) || within(lastByOrc, now, ORC_COOLDOWN_TICKS);
	}

	/** Un tiempo "futuro" (otro mundo abierto en la misma sesión) cuenta como expirado. */
	private static boolean within(Long last, long now, long cooldown) {
		return last != null && now >= last && now - last < cooldown;
	}

	private long pairCooldownTicks() {
		return runtime.config().dialogueCooldownSeconds * 20L;
	}

	/** Evita que los mapas crezcan sin límite en servidores que llevan mucho tiempo encendidos. */
	private void cleanUp(long now) {
		if (lastSpoken.size() > CLEANUP_THRESHOLD) {
			lastSpoken.values().removeIf(last -> !within(last, now, pairCooldownTicks()));
		}
		if (lastSpokenByOrc.size() > CLEANUP_THRESHOLD) {
			lastSpokenByOrc.values().removeIf(last -> !within(last, now, ORC_COOLDOWN_TICKS));
		}
	}

	private void broadcast(ServerLevel level, OrcEntity speaker, OrcProfile profile, DialogueLine line) {
		Component message = Component.translatable("chat.oromist-npc.orc_speech",
				com.oromist.entity.OrcDisplayNames.fullName(profile),
				toComponent(line).withStyle(ChatFormatting.ITALIC));
		double radius = runtime.config().dialogueRadius;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(speaker) <= radius * radius) {
				player.sendSystemMessage(message);
			}
		}
		level.playSound(null, speaker.getX(), speaker.getY(), speaker.getZ(), SoundEvents.PIGLIN_BRUTE_ANGRY,
				SoundSource.HOSTILE, 1.0F, speaker.getVoicePitch());
	}

	private static MutableComponent toComponent(DialogueLine line) {
		Object[] args = line.args().stream().map(ChatDialogueSpeaker::toArg).toArray();
		return Component.translatable(line.translationKey(), args);
	}

	private static Object toArg(DialogueLine.Arg arg) {
		return switch (arg) {
			case DialogueLine.Literal literal -> literal.text();
			case DialogueLine.Translatable translatable -> Component.translatable(translatable.key());
			case DialogueLine.Number number -> String.valueOf(number.value());
		};
	}

	private static String key(UUID orcId, UUID playerId) {
		return orcId + "/" + playerId;
	}
}
