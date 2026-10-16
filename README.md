# Elves (Fabric mod for Minecraft 1.21.1)

Elves inspired by classic high fantasy move into your villages. They walk with folded arms like villagers, leave you alone, and fight back if you hurt one of them.

| | Elf Warden | Elf Archer |
|---|---|---|
| Look | Dark hair, gold circlet, gold scale armor, deep green cloak | Blond braided hair, green tunic, grey-green cloak, quiver |
| Weapon | Iron sword (melee, 6 damage) | Bow (accurate arrows) |
| Share of spawns | about 65% | about 35% |

**Shared stats:** 30 health, 4 armor. Each elf drops 1 golden carrot and 5 XP, never its weapon.

## Behavior

- **Neutral, like an iron golem toward players.** Elves ignore you until you hit one. Then that elf, and every elf nearby, turns on you for about 20–40 seconds before calming down.
- **Villager-style.** They wander around their village and look at players and villagers. Arms stay folded until they fight; then the sword or bow comes out.
- **Never despawn.** Elves are residents, like villagers. Arrows from elves never hurt other elves.
- **Spawning.** Elves appear only in villages, near the bell. Every 20 seconds, each player within 64 blocks of a village gives a chance of a new elf arriving. Each village holds up to 4 elves. Spawning stops when the `doMobSpawning` gamerule is off.
- **Spawn egg.** There's an Elf Spawn Egg in the Spawn Eggs creative tab.

## What you need

1. **Java 21 (JDK).** Download "Temurin 21" from https://adoptium.net and install it.
2. **Fabric Loader for 1.21.1.** Get it from https://fabricmc.net/use/installer/.
3. **Fabric API for 1.21.1.** Download it from https://modrinth.com/mod/fabric-api and put the jar in your `mods` folder.

## Build the mod (one command)

Open a terminal in this folder. On Windows, open the folder in File Explorer, type `cmd` in the address bar, and press Enter.

- **Windows:** `gradlew build`
- **Mac/Linux:** `./gradlew build`

The first build downloads Minecraft and the Fabric tools, so it takes a few minutes. When it finishes, the mod is at:

```
build/libs/elves-1.0.0.jar
```

Copy that jar into your `.minecraft/mods` folder, next to Fabric API, then launch the game with the Fabric 1.21.1 profile.

**Test without your main game:** run `gradlew runClient` to launch a development copy of Minecraft with the mod loaded.

## Quick in-game test

1. Create a creative world and find a village, or use `/locate structure #minecraft:village`.
2. Use `/summon elves:elf`, or the spawn egg from the Spawn Eggs tab.
3. Hit an elf in survival mode to see the neutral behavior.

## Changing things

| What | Where |
|---|---|
| Health, speed, damage, armor | `ElfEntity.createElfAttributes()` |
| Archer chance (0.35 = 35%) | `ARCHER_CHANCE` in `ElfEntity.java` |
| Elves per village, spawn timing | `ElfVillageSpawner.java` |
| Drops | `src/main/resources/data/elves/loot_table/entities/elf.json` |
| Textures | `src/main/resources/assets/elves/textures/entity/elf/` (512×256, 4× detail) |
| Sounds | `src/main/resources/assets/elves/sounds/entity/elf/*.ogg` (mono .ogg) |

You can edit the textures in any image editor. To see them wrapped on the 3D model, open them in Blockbench: create a "Modded Entity" project and set the texture size to 128×64.

## Files

```
src/main/java/com/elvesmod/
  ElvesMod.java              mod entry point
  ModEntities.java           registers the Elf
  ModItems.java              spawn egg
  ModSounds.java             sound events
  entity/ElfEntity.java      AI, neutral anger, sword/bow combat, save data
  world/ElfVillageSpawner.java   spawns elves near village bells
src/client/java/com/elvesmod/client/
  ElfEntityModel.java        3D model (folded arms, pointed ears, cloak, long hair, quiver)
  ElfEntityRenderer.java     picks warden/archer texture, shows weapon while fighting
  ElvesClient.java, ModModelLayers.java
```
