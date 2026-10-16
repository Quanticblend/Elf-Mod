package com.elvesmod.world;

import com.elvesmod.ModEntities;
import com.elvesmod.entity.ElfEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Comparator;
import java.util.Optional;

/**
 * Spawns elves in villages. A village is found by its bell (the "meeting point"
 * that villagers gather around). Every 20 seconds, for each player near a village,
 * there's a chance one elf moves in, up to MAX_PER_VILLAGE elves per village.
 */
public final class ElfVillageSpawner {
    public static final int MAX_PER_VILLAGE = 4;
    public static final int HOME_RANGE = 32;
    private static final int CHECK_INTERVAL_TICKS = 400;
    private static final int PLAYER_SEARCH_RADIUS = 64;
    private static final int VILLAGE_RADIUS = 48;

    private ElfVillageSpawner() {}

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(ElfVillageSpawner::tick);
    }

    private static void tick(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD
                || world.getTime() % CHECK_INTERVAL_TICKS != 0
                || !world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            return;
        }
        Random random = world.getRandom();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            Optional<BlockPos> bell = findNearestBell(world, player.getBlockPos(), PLAYER_SEARCH_RADIUS);
            if (bell.isEmpty()) {
                continue;
            }
            int elves = world.getEntitiesByClass(ElfEntity.class, new Box(bell.get()).expand(VILLAGE_RADIUS), e -> true).size();
            if (elves < MAX_PER_VILLAGE && random.nextFloat() < 0.5F) {
                trySpawnNear(world, bell.get(), random);
            }
        }
    }

    public static Optional<BlockPos> findNearestBell(ServerWorld world, BlockPos center, int radius) {
        return world.getPointOfInterestStorage()
                .getInSquare(type -> type.matchesKey(PointOfInterestTypes.MEETING), center, radius,
                        PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .min(Comparator.comparingDouble(pos -> pos.getSquaredDistance(center)));
    }

    private static void trySpawnNear(ServerWorld world, BlockPos bell, Random random) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = bell.getX() + random.nextInt(25) - 12;
            int z = bell.getZ() + random.nextInt(25) - 12;
            BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, bell.getY(), z));
            BlockPos below = pos.down();
            boolean standable = world.getBlockState(below).isSolidBlock(world, below)
                    && world.isAir(pos) && world.isAir(pos.up());
            if (!standable) {
                continue;
            }
            ElfEntity elf = ModEntities.ELF.create(world);
            if (elf == null) {
                return;
            }
            elf.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            elf.setPositionTarget(bell, HOME_RANGE);
            elf.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
            world.spawnEntityAndPassengers(elf);
            return;
        }
    }
}
