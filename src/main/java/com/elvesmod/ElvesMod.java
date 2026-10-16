package com.elvesmod;

import com.elvesmod.world.ElfVillageSpawner;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElvesMod implements ModInitializer {
    public static final String MOD_ID = "elves";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModEntities.init();
        ModItems.init();
        ElfVillageSpawner.init();
        LOGGER.info("The elves have arrived.");
    }
}
