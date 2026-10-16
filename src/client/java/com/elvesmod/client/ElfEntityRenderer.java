package com.elvesmod.client;

import com.elvesmod.ElvesMod;
import com.elvesmod.entity.ElfEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ElfEntityRenderer extends MobEntityRenderer<ElfEntity, ElfEntityModel> {
    private static final Identifier WARDEN_TEXTURE = Identifier.of(ElvesMod.MOD_ID, "textures/entity/elf/elf_warden.png");
    private static final Identifier ARCHER_TEXTURE = Identifier.of(ElvesMod.MOD_ID, "textures/entity/elf/elf_archer.png");

    public ElfEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ElfEntityModel(context.getPart(ModModelLayers.ELF)), 0.5F);
        // Weapon is only drawn while fighting; at peace the elf walks with folded arms.
        this.addFeature(new HeldItemFeatureRenderer<ElfEntity, ElfEntityModel>(this, context.getHeldItemRenderer()) {
            @Override
            public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ElfEntity elf,
                               float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                               float headYaw, float headPitch) {
                if (elf.isAttacking()) {
                    super.render(matrices, vertexConsumers, light, elf, limbAngle, limbDistance, tickDelta,
                            animationProgress, headYaw, headPitch);
                }
            }
        });
    }

    @Override
    public Identifier getTexture(ElfEntity elf) {
        return elf.isArcher() ? ARCHER_TEXTURE : WARDEN_TEXTURE;
    }
}
