package com.elvesmod.client;

import com.elvesmod.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ElvesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ELF, ElfEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.ELF, ElfEntityRenderer::new);
    }
}
