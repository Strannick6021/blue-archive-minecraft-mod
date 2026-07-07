package com.strannick.companion.registry;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.Item;

import com.strannick.companion.CompanionMod;
import com.strannick.companion.item.ShotgunItem;
import com.strannick.companion.item.ShieldItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CompanionMod.MODID);

    // Оружие
    public static final RegistryObject<Item> SHOTGUN = ITEMS.register("shotgun", ShotgunItem::new);
    public static final RegistryObject<Item> SHIELD = ITEMS.register("shield", ShieldItem::new);
}