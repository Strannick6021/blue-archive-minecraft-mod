package com.strannick.companion.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "companion", bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompanionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue SHOTGUN_DAMAGE;
    public static final ForgeConfigSpec.IntValue SHOTGUN_PELLETS;
    public static final ForgeConfigSpec.IntValue SHOTGUN_MAX_AMMO;
    public static final ForgeConfigSpec.IntValue SHOTGUN_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue COMPANION_HEALTH;
    public static final ForgeConfigSpec.DoubleValue COMPANION_SPEED;
    public static final ForgeConfigSpec.IntValue COMPANION_SEARCH_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Shotgun Configuration");
        SHOTGUN_DAMAGE = builder
                .comment("Damage per pellet")
                .defineInRange("shotgun_damage", 12, 1, 100);
        
        SHOTGUN_PELLETS = builder
                .comment("Number of pellets per shot")
                .defineInRange("shotgun_pellets", 8, 1, 20);
        
        SHOTGUN_MAX_AMMO = builder
                .comment("Maximum ammo in magazine")
                .defineInRange("shotgun_max_ammo", 8, 1, 32);
        
        SHOTGUN_COOLDOWN = builder
                .comment("Cooldown between shots (in ticks)")
                .defineInRange("shotgun_cooldown", 20, 5, 100);

        builder.comment("Companion Configuration");
        COMPANION_HEALTH = builder
                .comment("Companion max health")
                .defineInRange("companion_health", 30.0, 10.0, 200.0);
        
        COMPANION_SPEED = builder
                .comment("Companion movement speed")
                .defineInRange("companion_speed", 0.4, 0.1, 1.0);
        
        COMPANION_SEARCH_RADIUS = builder
                .comment("Search radius for mining and enemies")
                .defineInRange("companion_search_radius", 32, 16, 64);

        SPEC = builder.build();
    }
}