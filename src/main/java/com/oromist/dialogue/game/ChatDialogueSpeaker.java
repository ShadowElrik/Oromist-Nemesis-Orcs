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
	private final Map<String, Long> lastSpoken = new HashMap<>();

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
		});
	}

	private boolean isOnCooldown(UUID orcId, UUID playerId, long now) {
		Long last = lastSpoken.get(key(orcId, playerId));
		long cooldownTicks = runtime.config().dialogueCooldownSeconds * 20L;
		return last != null && now - last < cooldownTicks;
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
