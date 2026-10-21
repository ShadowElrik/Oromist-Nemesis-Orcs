package com.oromist.nemesis.game;

import com.oromist.dialogue.DialogueTrigger;
import com.oromist.dialogue.game.DialogueSpeaker;
import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcHooks;
import com.oromist.nemesis.domain.NemesisService;
import com.oromist.nemesis.domain.OrcProfile;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Implementación de {@link OrcHooks} que conecta el comportamiento del orco con el
 * sistema Némesis: sincroniza su perfil, registra recuerdos y lo hace hablar.
 */
public final class OrcNemesisHooks implements OrcHooks {
	private final NemesisRuntime runtime;
	private final DialogueSpeaker speaker;

	public OrcNemesisHooks(NemesisRuntime runtime, DialogueSpeaker speaker) {
		this.runtime = runtime;
		this.speaker = speaker;
	}

	@Override
	public void onServerSecond(OrcEntity orc) {
		runtime.service().ifPresent(service -> {
			if (!syncProfile(orc, service)) {
				return;
			}
			if (orc.getTarget() instanceof ServerPlayer player) {
				OrcProfiles.of(orc, service).ifPresent(profile -> greet(orc, profile, player, service));
			}
		});
	}

	/**
	 * Mantiene la entidad coherente con su perfil.
	 *
	 * @return {@code false} si la entidad se eliminó por estar obsoleta
	 */
	private boolean syncProfile(OrcEntity orc, NemesisService service) {
		if (orc.profileId().isEmpty()) {
			// Capitanes generados con la aldea: su perfil se crea ya en el hilo principal.
			if (orc.pendingRank().isPresent()) {
				OrcProfiles.ensure(orc, service);
			}
			return true;
		}
		Optional<OrcProfile> profile = OrcProfiles.of(orc, service);
		boolean stale = profile.isEmpty() || !profile.get().isAlive() || profile.get().incarnation() != orc.incarnation();
		if (stale) {
			// Es un "eco": el orco murió o reapareció en otro lugar (emboscada).
			orc.discard();
			return false;
		}
		if (!orc.isProfileApplied()) {
			orc.applyProfile(profile.get());
		}
		return true;
	}

	private void greet(OrcEntity orc, OrcProfile profile, ServerPlayer player, NemesisService service) {
		if (orc.distanceTo(player) > 16) {
			return;
		}
		boolean firstTime = service.recordEncounter(profile, Encounters.of(player));
		if (firstTime && !profile.rank().isNamed()) {
			return; // Los soldados rasos no se presentan, solo hablan cuando ya te conocen.
		}
		speaker.speak(orc, player, DialogueTrigger.SIGHTED);
	}

	@Override
	public void onRetreatStarted(OrcEntity orc, LivingEntity threat) {
		if (threat instanceof ServerPlayer player) {
			runtime.service().ifPresent(service -> {
				OrcProfiles.ensure(orc, service);
				speaker.speak(orc, player, DialogueTrigger.FLEEING);
			});
		}
	}

	@Override
	public void onEscaped(OrcEntity orc, LivingEntity threat) {
		if (threat instanceof ServerPlayer player) {
			runtime.service().ifPresent(service ->
					service.onOrcFled(OrcProfiles.ensure(orc, service), Encounters.of(player)));
		}
	}

	@Override
	public void onTargetFled(OrcEntity orc, ServerPlayer player) {
		runtime.service().ifPresent(service -> {
			OrcProfile profile = OrcProfiles.ensure(orc, service);
			service.onPlayerFled(profile, Encounters.of(player));
			orc.applyProfile(profile);
		});
	}
}
