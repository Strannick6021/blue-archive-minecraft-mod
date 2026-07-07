package com.strannick.companion.registry;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.strannick.companion.CompanionMod;
import com.strannick.companion.entity.CompanionEntity;
import com.strannick.companion.entity.ShotgunProjectile;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CompanionMod.MODID);

    // Сущность компаньона
    public static final RegistryObject<EntityType<CompanionEntity>> COMPANION = ENTITIES.register("companion",
            () -> EntityType.Builder.of(CompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .setTrackingRange(80)
                    .setUpdateInterval(3)
                    .build("companion"));

    // Снаряд дробовика
    public static final RegistryObject<EntityType<ShotgunProjectile>> SHOTGUN_PROJECTILE = ENTITIES.register("shotgun_projectile",
            () -> EntityType.Builder.<ShotgunProjectile>of(ShotgunProjectile::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .build("shotgun_projectile"));
}