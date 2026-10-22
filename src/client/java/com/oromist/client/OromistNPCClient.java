package com.oromist.client;

import com.oromist.client.render.OrcModel;
import com.oromist.client.render.OrcRenderer;
import com.oromist.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** Punto de entrada del cliente: registra modelos y renderizadores del mod. */
public class OromistNPCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(OrcModel.LAYER, OrcModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.ORC, OrcRenderer::new);
		// El fuego verde se dibuja como su objeto (una bola de slime), igual que las bolas de fuego vanilla.
		EntityRendererRegistry.register(ModEntities.GREEN_FIREBALL, context -> new ThrownItemRenderer<>(context, 0.75F, true));
	}
}
