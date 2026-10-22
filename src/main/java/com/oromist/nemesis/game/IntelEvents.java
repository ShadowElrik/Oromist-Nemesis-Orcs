package com.oromist.nemesis.game;

import com.oromist.dialogue.DialogueTrigger;
import com.oromist.dialogue.game.DialogueSpeaker;
import com.oromist.entity.OrcEntity;
import com.oromist.nemesis.domain.IntelService;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.registry.ModItems;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Las dos formas activas de conseguir información de guerra:
 * <ul>
 *   <li><b>Interrogar</b>: agachado, clic derecho sobre un orco malherido. Un soldado raso
 *       delata un rasgo de otro orco (preferiblemente de tu némesis) y huye; un capitán o
 *       caudillo se niega y sigue luchando. El interrogado lo recordará.</li>
 *   <li><b>Notas de guerra</b>: se leen con clic derecho y revelan un rasgo.</li>
 * </ul>
 */
public final class IntelEvents {
	private final NemesisRuntime runtime;
	private final DialogueSpeaker speaker;

	public IntelEvents(NemesisRuntime runtime, DialogueSpeaker speaker) {
		this.runtime = runtime;
		this.speaker = speaker;
	}

	public void register() {
		UseEntityCallback.EVENT.register(this::onUseEntity);
		UseItemCallback.EVENT.register(this::onUseItem);
	}

	// --- Interrogatorio ----------------------------------------------------------------

	private InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit) {
		if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
				|| !(entity instanceof OrcEntity orc) || !orc.isBroken()) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			runtime.service().ifPresent(service -> runtime.intel().ifPresent(intel ->
					interrogate(orc, serverPlayer, service, intel)));
		}
		// También en el cliente, para que el clic no se convierta en otra acción.
		return InteractionResult.SUCCESS;
	}

	private void interrogate(OrcEntity orc, ServerPlayer player, NemesisService service, IntelService intel) {
		if (orc.wasInterrogated()) {
			player.sendSystemMessage(IntelMessages.nothingNew());
			return;
		}
		orc.markInterrogated();
		if (orc.currentRank().isNamed()) {
			// Un capitán no delata a los suyos: escupe y sigue peleando.
			OrcProfiles.ensure(orc, service);
			speaker.speak(orc, player, DialogueTrigger.INTERROGATION_REFUSE);
			orc.setTarget(player);
			return;
		}
		OrcProfile informant = OrcProfiles.ensure(orc, service);
		service.onInterrogated(informant, Encounters.of(player));
		speaker.speak(orc, player, DialogueTrigger.INTERROGATION_TALK);
		Optional<IntelService.Intel> secret = intel.extractIntel(player.getUUID(), Optional.of(informant.id()));
		player.sendSystemMessage(secret.map(IntelMessages::informant).orElseGet(IntelMessages::nothingNew));
		orc.forceRetreat();
	}

	// --- Notas de guerra -----------------------------------------------------------------

	private InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(ModItems.WAR_NOTES)) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			runtime.intel().ifPresent(intel -> readNotes(serverPlayer, stack, intel));
		}
		return InteractionResult.SUCCESS;
	}

	private static void readNotes(ServerPlayer player, ItemStack notes, IntelService intel) {
		Optional<IntelService.Intel> secret = intel.extractIntel(player.getUUID());
		if (secret.isEmpty()) {
			// No se gastan: quizá sirvan cuando haya nuevos capitanes.
			player.sendSystemMessage(IntelMessages.nothingNew());
			return;
		}
		player.sendSystemMessage(IntelMessages.fromNotes(secret.get()));
		notes.consume(1, player);
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN,
				SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
