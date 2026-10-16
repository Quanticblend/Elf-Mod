package com.elvesmod;

import com.elvesmod.entity.ElfEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<ElfEntity> ELF = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(ElvesMod.MOD_ID, "elf"),
            EntityType.Builder.create(ElfEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6F, 1.95F)
                    .eyeHeight(1.62F)
                    .maxTrackingRange(10)
                    .build("elf"));

    private ModEntities() {}

    public static void init() {
        FabricDefaultAttributeRegistry.register(ELF, ElfEntity.createElfAttributes());
    }
}
