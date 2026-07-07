package com.strannick.companion;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafxmod.FXModLanguageProvider;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strannick.companion.registry.ModEntities;
import com.strannick.companion.registry.ModItems;
import com.strannick.companion.registry.ModSounds;
import com.strannick.companion.event.CompanionEvents;

@Mod("companion")
public class CompanionMod {
    public static final String MODID = "companion";
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionMod.class);

    public CompanionMod(FXModLanguageProvider.ModContainer modContainer) {
        IEventBus modEventBus = modContainer.getEventBus();

        // Регистрируем события
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }

        // Регистрируем отложенные реестры
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        // Регистрируем события
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.strannick.companion.config.CompanionConfig.SPEC
        );

        LOGGER.info("CompanionMod инициализирован!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Общая ин��циализация CompanionMod");
        event.enqueueWork(() -> {
            // Регистрируем кастомные приказы компаньона
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Клиентская инициализация CompanionMod");
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        LOGGER.info("CompanionMod полностью загружен!");
    }
}