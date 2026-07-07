package com.strannick.companion.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

import com.strannick.companion.CompanionMod;
import com.strannick.companion.entity.CompanionEntity;
import com.strannick.companion.entity.ShotgunProjectile;
import com.strannick.companion.client.render.CompanionRenderer;
import com.strannick.companion.client.render.ShotgunProjectileRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@Mod.EventBusSubscriber(modid = CompanionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompanionEvents {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.Register event) {
        // Регистрируем рендер компаньона
        event.registerEntityRenderer(com.strannick.companion.registry.ModEntities.COMPANION.get(),
                context -> new CompanionRenderer(context));
        
        // Регистрируем рендер снаряда дробовика
        event.registerEntityRenderer(com.strannick.companion.registry.ModEntities.SHOTGUN_PROJECTILE.get(),
                context -> new ThrownItemRenderer(context));
    }
}