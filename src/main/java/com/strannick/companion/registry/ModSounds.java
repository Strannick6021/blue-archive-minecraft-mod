package com.strannick.companion.registry;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import com.strannick.companion.CompanionMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CompanionMod.MODID);

    // Звуки оружия
    public static final RegistryObject<SoundEvent> SHOTGUN_FIRE = SOUNDS.register("shotgun_fire",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "shotgun_fire")));

    public static final RegistryObject<SoundEvent> SHOTGUN_RELOAD = SOUNDS.register("shotgun_reload",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "shotgun_reload")));

    // Звуки компаньона
    public static final RegistryObject<SoundEvent> COMPANION_AFFIRM = SOUNDS.register("companion_affirm",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "companion_affirm")));

    public static final RegistryObject<SoundEvent> COMPANION_DENY = SOUNDS.register("companion_deny",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "companion_deny")));

    public static final RegistryObject<SoundEvent> COMPANION_ATTACK = SOUNDS.register("companion_attack",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "companion_attack")));

    public static final RegistryObject<SoundEvent> COMPANION_MINING = SOUNDS.register("companion_mining",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CompanionMod.MODID, "companion_mining")));
}