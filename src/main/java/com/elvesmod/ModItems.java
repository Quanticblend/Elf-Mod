package com.elvesmod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item ELF_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(ElvesMod.MOD_ID, "elf_spawn_egg"),
            new SpawnEggItem(ModEntities.ELF, 0x2E4A33, 0xD9B44A, new Item.Settings()));

    private ModItems() {}

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(ELF_SPAWN_EGG));
    }
}
