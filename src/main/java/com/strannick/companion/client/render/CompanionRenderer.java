package com.strannick.companion.client.render;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;

import com.strannick.companion.CompanionMod;
import com.strannick.companion.entity.CompanionEntity;

public class CompanionRenderer extends MobRenderer<CompanionEntity, HumanoidModel<CompanionEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CompanionMod.MODID, "textures/entity/companion/companion.png");

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return TEXTURE;
    }
}