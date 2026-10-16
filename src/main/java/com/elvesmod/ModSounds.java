package com.elvesmod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    public static final SoundEvent ELF_AMBIENT = register("entity.elf.ambient");
    public static final SoundEvent ELF_HURT = register("entity.elf.hurt");
    public static final SoundEvent ELF_DEATH = register("entity.elf.death");

    private ModSounds() {}

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(ElvesMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init() {
        // Loading the class registers the sounds.
    }
}
