package com.strannick.companion.client.render;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.strannick.companion.entity.ShotgunProjectile;

public class ShotgunProjectileRenderer extends ThrownItemRenderer<ShotgunProjectile> {
    public ShotgunProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}