package com.oromist.client.render;

import com.oromist.OromistNPC;
import com.oromist.entity.OrcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Dibuja al orco: elige la textura según su variante de piel, muestra armadura y
 * coloca los brazos para apuntar con arco o cargar/sostener la ballesta.
 * La altura variable la aplica Minecraft automáticamente a partir del atributo SCALE.
 */
public class OrcRenderer extends HumanoidMobRenderer<OrcEntity, OrcRenderState, OrcModel> {
	private static final Identifier[] TEXTURES = new Identifier[OrcEntity.SKIN_VARIANTS];

	static {
		for (int i = 0; i < TEXTURES.length; i++) {
			TEXTURES[i] = OromistNPC.id("textures/entity/orc/orc_" + i + ".png");
		}
	}

	public OrcRenderer(EntityRendererProvider.Context context) {
		super(context, new OrcModel(context.bakeLayer(OrcModel.LAYER)), 0.5F);
		addLayer(new HumanoidArmorLayer<>(this,
				ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), part -> new HumanoidModel<OrcRenderState>(part)),
				context.getEquipmentRenderer()));
	}

	@Override
	public OrcRenderState createRenderState() {
		return new OrcRenderState();
	}

	@Override
	public void extractRenderState(OrcEntity entity, OrcRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.skinVariant = entity.skinVariant();
	}

	@Override
	public Identifier getTextureLocation(OrcRenderState state) {
		return TEXTURES[Math.floorMod(state.skinVariant, TEXTURES.length)];
	}

	@Override
	protected HumanoidModel.ArmPose getArmPose(OrcEntity orc, HumanoidArm arm) {
		HumanoidModel.ArmPose mainPose = mainHandPose(orc);
		if (arm == orc.getMainArm()) {
			return mainPose != null ? mainPose : super.getArmPose(orc, arm);
		}
		if (mainPose != null && mainPose.isTwoHanded()) {
			// Con arco o ballesta, la otra mano acompaña al arma.
			ItemStack offHand = orc.getOffhandItem();
			return offHand.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
		}
		return super.getArmPose(orc, arm);
	}

	/** Pose de la mano del arma, o {@code null} si se usa la pose por defecto. */
	private static HumanoidModel.ArmPose mainHandPose(OrcEntity orc) {
		ItemStack weapon = orc.getMainHandItem();
		if (weapon.is(Items.CROSSBOW)) {
			if (orc.isChargingCrossbow()) {
				return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
			}
			boolean aiming = CrossbowItem.isCharged(weapon) || orc.isAggressive();
			return aiming ? HumanoidModel.ArmPose.CROSSBOW_HOLD : HumanoidModel.ArmPose.ITEM;
		}
		if (weapon.is(Items.BOW) && orc.isAggressive()) {
			return HumanoidModel.ArmPose.BOW_AND_ARROW;
		}
		return null;
	}
}
