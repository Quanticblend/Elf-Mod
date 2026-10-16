package com.elvesmod.client;

import com.elvesmod.ElvesMod;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModModelLayers {
    public static final EntityModelLayer ELF = new EntityModelLayer(Identifier.of(ElvesMod.MOD_ID, "elf"), "main");

    private ModModelLayers() {}
}
