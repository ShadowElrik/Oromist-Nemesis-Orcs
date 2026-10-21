package com.oromist.client;

import com.oromist.client.render.OrcModel;
import com.oromist.client.render.OrcRenderer;
import com.oromist.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

/** Punto de entrada del cliente: registra modelo y renderizador del orco. */
public class OromistNPCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(OrcModel.LAYER, OrcModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.ORC, OrcRenderer::new);
	}
}
