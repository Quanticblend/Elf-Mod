package com.elvesmod.entity;

import com.elvesmod.ModSounds;
import com.elvesmod.world.ElfVillageSpawner;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A neutral village elf.
 *
 * Wardens carry a sword and fight up close; archers carry a bow and shoot from range.
 * Like a villager, an elf wanders peacefully around its village. It only fights when
 * someone hurts it (or hurts an elf near it), and calms down after a while.
 */
public class ElfEntity extends PathAwareEntity implements Angerable, RangedAttackMob {
    public static final int WARDEN = 0;
    public static final int ARCHER = 1;
    /** Chance that a newly spawned elf is an archer. */
    private static final float ARCHER_CHANCE = 0.35F;

    private static final TrackedData<Integer> VARIANT =
            DataTracker.registerData(ElfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);

    private int angerTime;
    @Nullable private UUID angryAt;
    @Nullable private MeleeAttackGoal meleeGoal;
    @Nullable private ProjectileAttackGoal rangedGoal;

    public ElfEntity(EntityType<? extends ElfEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 5;
        // Elves keep their weapons; the only drop is the golden carrot from the loot table.
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public static DefaultAttributeContainer.Builder createElfAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0) // + iron sword = 6 damage
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    // ---------------------------------------------------------------- AI

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        // Priority 2 is filled by the melee or ranged attack goal in updateAttackType().
        this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.8));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.55));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAtEntityGoal(this, VillagerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));

        // Neutral: fight back when hurt, and call nearby elves to help.
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
    }

    /** Swaps between the sword and bow fighting styles to match the variant. */
    public void updateAttackType() {
        if (this.getWorld() == null || this.getWorld().isClient) {
            return;
        }
        if (this.meleeGoal == null || this.rangedGoal == null) {
            this.meleeGoal = new MeleeAttackGoal(this, 1.0, true);
            this.rangedGoal = new ProjectileAttackGoal(this, 1.0, 30, 16.0F);
        }
        this.goalSelector.remove(this.meleeGoal);
        this.goalSelector.remove(this.rangedGoal);
        this.goalSelector.add(2, this.isArcher() ? this.rangedGoal : this.meleeGoal);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld) this.getWorld(), true);
            // Synced to the client: arms uncross and the weapon comes out while fighting.
            LivingEntity target = this.getTarget();
            this.setAttacking(target != null && target.isAlive());
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack bow = this.getMainHandStack().isOf(Items.BOW) ? this.getMainHandStack() : new ItemStack(Items.BOW);
        ArrowEntity arrow = new ArrowEntity(this.getWorld(), this, new ItemStack(Items.ARROW), bow);
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        // Elves are sharper shots than skeletons.
        float inaccuracy = Math.max(0.0F, 6.0F - this.getWorld().getDifficulty().getId() * 2.0F);
        arrow.setVelocity(dx, dy + horizontal * 0.2, dz, 1.6F, inaccuracy);
        arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(arrow);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Stray arrows from fellow elves never hurt (or anger) an elf.
        if (source.getAttacker() instanceof ElfEntity) {
            return false;
        }
        return super.damage(source, amount);
    }

    // ---------------------------------------------------------------- Spawning

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setVariant(this.random.nextFloat() < ARCHER_CHANCE ? ARCHER : WARDEN);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(this.isArcher() ? Items.BOW : Items.IRON_SWORD));
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        this.setLeftHanded(false);
        this.updateAttackType();

        // Adopt the nearest village as home so the elf stays around it, like a villager.
        if (!this.hasPositionTarget()) {
            ElfVillageSpawner.findNearestBell(world.toServerWorld(), this.getBlockPos(), 48)
                    .ifPresent(bell -> this.setPositionTarget(bell, ElfVillageSpawner.HOME_RANGE));
        }
        return data;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false; // Elves are village residents and never despawn.
    }

    // ---------------------------------------------------------------- Variant

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, WARDEN);
    }

    public int getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant == ARCHER ? ARCHER : WARDEN);
    }

    public boolean isArcher() {
        return this.getVariant() == ARCHER;
    }

    // ---------------------------------------------------------------- Save data

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant());
        if (this.hasPositionTarget()) {
            BlockPos home = this.getPositionTarget();
            nbt.putInt("HomeX", home.getX());
            nbt.putInt("HomeY", home.getY());
            nbt.putInt("HomeZ", home.getZ());
            nbt.putInt("HomeRange", (int) this.getPositionTargetRange());
        }
        this.writeAngerToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setVariant(nbt.getInt("Variant"));
        if (nbt.contains("HomeX")) {
            this.setPositionTarget(
                    new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ")),
                    nbt.getInt("HomeRange"));
        }
        this.readAngerFromNbt(this.getWorld(), nbt);
        this.updateAttackType();
    }

    // ---------------------------------------------------------------- Sounds

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return ModSounds.ELF_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ELF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ELF_DEATH;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 240; // Elves speak softly and not too often.
    }

    // ---------------------------------------------------------------- Anger (neutral behavior)

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Override
    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }
}
