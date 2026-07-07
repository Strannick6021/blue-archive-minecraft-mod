package com.strannick.companion;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strannick.companion.registry.ModEntities;
import com.strannick.companion.registry.ModItems;
import com.strannick.companion.registry.ModSounds;

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

        LOGGER.info("CompanionMod инициализирован!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Общая инициализация CompanionMod");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Клиентская инициализация CompanionMod");
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        LOGGER.info("CompanionMod полностью загружен!");
    }
}