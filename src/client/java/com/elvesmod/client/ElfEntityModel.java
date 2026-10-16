package com.elvesmod.client;

import com.elvesmod.entity.ElfEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

/**
 * Elf model. Villager-style body: arms folded across the chest while walking,
 * legs swinging under a long cloak. When fighting, the folded arms are swapped for
 * free arms holding a sword (wardens) or drawing a bow (archers).
 *
 * The texture layout is 128x64 units. The PNG files are painted at 4x that size
 * (512x256) for a higher-detail look; Minecraft scales the UVs automatically.
 */
public class ElfEntityModel extends SinglePartEntityModel<ElfEntity> implements ModelWithArms, ModelWithHead {
    private static final float DEG = (float) (Math.PI / 180.0);
    private static final float PI = (float) Math.PI;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart quiver;
    private final ModelPart arms;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ElfEntityModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.quiver = root.getChild("body").getChild("quiver");
        this.arms = root.getChild("arms");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Head with a hair layer slightly larger than the skull.
        ModelPartData head = root.addChild("head", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.25F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        // Long hair falling down the back.
        head.addChild("hair_back", ModelPartBuilder.create()
                        .uv(64, 0).cuboid(-4.0F, -7.0F, 4.0F, 8.0F, 13.0F, 1.0F),
                ModelTransform.NONE);
        // Pointed ears, swept back and up.
        head.addChild("right_ear", ModelPartBuilder.create()
                        .uv(84, 0).cuboid(-3.0F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
                ModelTransform.of(-3.5F, -3.5F, 0.5F, 0.0F, 0.35F, 0.5F));
        head.addChild("left_ear", ModelPartBuilder.create()
                        .uv(84, 4).mirrored().cuboid(0.0F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
                ModelTransform.of(3.5F, -3.5F, 0.5F, 0.0F, -0.35F, -0.5F));

        // Body (tunic or armor) plus a long cloak that is open at the front.
        ModelPartData body = root.addChild("body", ModelPartBuilder.create()
                        .uv(16, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .uv(0, 38).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        // Quiver slung across the back (only shown on archers).
        body.addChild("quiver", ModelPartBuilder.create()
                        .uv(96, 0).cuboid(-1.5F, -6.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                ModelTransform.of(1.5F, 6.0F, 5.5F, 0.0F, 0.0F, -0.5F));

        // Folded arms, exactly like a villager's.
        ModelPartData arms = root.addChild("arms", ModelPartBuilder.create()
                        .uv(44, 22).cuboid(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .uv(40, 38).cuboid(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                ModelTransform.of(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
        arms.addChild("left_shoulder", ModelPartBuilder.create()
                        .uv(44, 22).mirrored().cuboid(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                ModelTransform.NONE);

        // Free arms used while fighting.
        root.addChild("right_arm", ModelPartBuilder.create()
                        .uv(40, 46).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
        root.addChild("left_arm", ModelPartBuilder.create()
                        .uv(40, 46).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F));

        root.addChild("right_leg", ModelPartBuilder.create()
                        .uv(0, 22).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
        root.addChild("left_leg", ModelPartBuilder.create()
                        .uv(0, 22).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(2.0F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 128, 64);
    }

    @Override
    public void setAngles(ElfEntity elf, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.yaw = headYaw * DEG;
        this.head.pitch = headPitch * DEG;

        // Villager walk: legs swing, body stays upright.
        if (this.riding) {
            this.rightLeg.pitch = -1.4137167F;
            this.rightLeg.yaw = PI / 10.0F;
            this.leftLeg.pitch = -1.4137167F;
            this.leftLeg.yaw = -PI / 10.0F;
        } else {
            this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance * 0.5F;
            this.rightLeg.yaw = 0.0F;
            this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + PI) * 1.4F * limbDistance * 0.5F;
            this.leftLeg.yaw = 0.0F;
        }

        this.quiver.visible = elf.isArcher();

        boolean fighting = elf.isAttacking();
        this.arms.visible = !fighting;
        this.rightArm.visible = fighting;
        this.leftArm.visible = fighting;

        this.rightArm.pitch = 0.0F;
        this.rightArm.yaw = 0.0F;
        this.rightArm.roll = 0.0F;
        this.leftArm.pitch = 0.0F;
        this.leftArm.yaw = 0.0F;
        this.leftArm.roll = 0.0F;

        if (!fighting) {
            return;
        }

        if (elf.isArcher()) {
            // Bow drawn and aimed where the elf is looking.
            this.rightArm.yaw = -0.1F + this.head.yaw;
            this.rightArm.pitch = -PI / 2.0F + this.head.pitch;
            this.leftArm.yaw = 0.1F + this.head.yaw + 0.4F;
            this.leftArm.pitch = -PI / 2.0F + this.head.pitch;
        } else {
            // Sword held ready, arms swinging with the stride.
            float stride = MathHelper.cos(limbAngle * 0.6662F) * limbDistance;
            this.rightArm.pitch = -0.35F - stride * 0.4F;
            this.leftArm.pitch = stride * 0.6F;
            this.rightArm.roll = MathHelper.cos(animationProgress * 0.09F) * 0.05F + 0.05F;
            this.leftArm.roll = -(MathHelper.cos(animationProgress * 0.09F) * 0.05F + 0.05F);

            // Sword strike.
            if (this.handSwingProgress > 0.0F) {
                float f = 1.0F - this.handSwingProgress;
                f *= f;
                f *= f;
                f = 1.0F - f;
                float lift = MathHelper.sin(f * PI);
                float follow = MathHelper.sin(this.handSwingProgress * PI) * -(this.head.pitch - 0.7F) * 0.75F;
                this.rightArm.pitch -= lift * 1.2F + follow;
                this.rightArm.roll += MathHelper.sin(this.handSwingProgress * PI) * -0.4F;
            }
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        (arm == Arm.LEFT ? this.leftArm : this.rightArm).rotate(matrices);
    }
}
