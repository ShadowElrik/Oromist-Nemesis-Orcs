package com.oromist.entity;

import com.oromist.OromistNPC;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Traduce cada {@link OrcTrait} del dominio a efectos concretos de Minecraft.
 * Para añadir un rasgo: crear el valor en el enum y registrar aquí su efecto.
 */
public final class TraitEffects {
	private static final Map<OrcTrait, TraitEffect> EFFECTS = new EnumMap<>(OrcTrait.class);

	static {
		EFFECTS.put(OrcTrait.BRUTE, attribute(Attributes.ATTACK_DAMAGE, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		EFFECTS.put(OrcTrait.SWIFT, attribute(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		EFFECTS.put(OrcTrait.THICK_HIDE, new TraitEffect() {
			@Override
			public void apply(OrcEntity orc, Identifier id) {
				addModifier(orc, Attributes.MAX_HEALTH, new AttributeModifier(id, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
				addModifier(orc, Attributes.ARMOR, new AttributeModifier(id, 4.0, AttributeModifier.Operation.ADD_VALUE));
			}
		});
		EFFECTS.put(OrcTrait.ARROW_WARD, damageMultiplier(source -> source.is(DamageTypeTags.IS_PROJECTILE), 0.25f));
		EFFECTS.put(OrcTrait.FEARS_FIRE, damageMultiplier(source -> source.is(DamageTypeTags.IS_FIRE), 2.0f));
		EFFECTS.put(OrcTrait.FRAIL, damageMultiplier(
				source -> source.getEntity() != null && source.getDirectEntity() == source.getEntity(), 1.5f));
		EFFECTS.put(OrcTrait.BERSERKER, new TraitEffect() {
			@Override
			public void tick(OrcEntity orc, Identifier id) {
				AttributeInstance damage = orc.getAttribute(Attributes.ATTACK_DAMAGE);
				if (damage == null) {
					return;
				}
				boolean enraged = orc.getHealth() < orc.getMaxHealth() * 0.4f;
				if (enraged && !damage.hasModifier(id)) {
					damage.addTransientModifier(new AttributeModifier(id, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
				} else if (!enraged && damage.hasModifier(id)) {
					damage.removeModifier(id);
				}
			}
		});
		// COWARD no necesita efecto de atributos: lo consulta OrcEntity#wantsToRetreat.
	}

	private TraitEffects() {
	}

	public static void applyAll(OrcEntity orc) {
		for (OrcTrait trait : orc.traits()) {
			effect(trait).apply(orc, idFor(trait));
		}
	}

	public static void tickAll(OrcEntity orc) {
		for (OrcTrait trait : orc.traits()) {
			effect(trait).tick(orc, idFor(trait));
		}
	}

	public static float modifyIncomingDamage(OrcEntity orc, DamageSource source, float amount) {
		float result = amount;
		for (OrcTrait trait : orc.traits()) {
			result = effect(trait).modifyIncomingDamage(source, result);
		}
		return result;
	}

	private static TraitEffect effect(OrcTrait trait) {
		return EFFECTS.getOrDefault(trait, TraitEffect.NONE);
	}

	private static Identifier idFor(OrcTrait trait) {
		return OromistNPC.id("trait/" + trait.name().toLowerCase(Locale.ROOT));
	}

	private static void addModifier(OrcEntity orc, Holder<Attribute> attribute, AttributeModifier modifier) {
		AttributeInstance instance = orc.getAttribute(attribute);
		if (instance != null) {
			instance.addOrReplacePermanentModifier(modifier);
		}
	}

	private static TraitEffect attribute(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
		return new TraitEffect() {
			@Override
			public void apply(OrcEntity orc, Identifier id) {
				addModifier(orc, attribute, new AttributeModifier(id, amount, operation));
			}
		};
	}

	private static TraitEffect damageMultiplier(java.util.function.Predicate<DamageSource> condition, float multiplier) {
		return new TraitEffect() {
			@Override
			public float modifyIncomingDamage(DamageSource source, float amount) {
				return condition.test(source) ? amount * multiplier : amount;
			}
		};
	}

	/** Efecto de un rasgo; cada método tiene una implementación vacía por defecto. */
	interface TraitEffect {
		TraitEffect NONE = new TraitEffect() {
		};

		/** Modificadores permanentes, al vincular el perfil. */
		default void apply(OrcEntity orc, Identifier id) {
		}

		/** Efectos dinámicos, una vez por segundo. */
		default void tick(OrcEntity orc, Identifier id) {
		}

		default float modifyIncomingDamage(DamageSource source, float amount) {
			return amount;
		}
	}
}
