package com.oromist.client.render;

import com.oromist.OromistNPC;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modelo humanoide con mandíbula prominente, colmillos y orejas puntiagudas.
 * Reutiliza las animaciones de {@link HumanoidModel} (andar, golpear, apuntar con
 * arco y ballesta) y usa una textura con el formato de skin de jugador (64x64).
 */
public class OrcModel extends HumanoidModel<OrcRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(OromistNPC.id("orc"), "main");

	public OrcModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition head = mesh.getRoot().getChild("head");
		// Las coordenadas de textura usan la zona inferior del skin (v >= 32), libre en HumanoidModel.
		head.addOrReplaceChild("jaw",
				CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -2.5F, -5.0F, 6.0F, 2.0F, 1.0F),
				PartPose.ZERO);
		head.addOrReplaceChild("tusks",
				CubeListBuilder.create()
						.texOffs(0, 32).addBox(-3.0F, -4.5F, -5.5F, 1.0F, 2.0F, 1.0F)
						.texOffs(4, 32).addBox(2.0F, -4.5F, -5.5F, 1.0F, 2.0F, 1.0F),
				PartPose.ZERO);
		head.addOrReplaceChild("ears",
				CubeListBuilder.create()
						.texOffs(16, 32).addBox(-6.0F, -6.0F, -1.0F, 2.0F, 3.0F, 1.0F)
						.texOffs(16, 36).mirror().addBox(4.0F, -6.0F, -1.0F, 2.0F, 3.0F, 1.0F),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}
}
