package com.strannick.companion.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.nbt.CompoundTag;

public class ShotgunProjectile extends Projectile {
    private static final int MAX_LIFETIME = 200;
    private float damage;
    private float range;
    private int lifespan = 0;

    public ShotgunProjectile(Level level, LivingEntity owner, float damage, float range) {
        this(null, level);
        this.setOwner(owner);
        this.damage = damage;
        this.range = range;
    }

    public ShotgunProjectile(EntityType<? extends ShotgunProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        lifespan++;
        if (lifespan > MAX_LIFETIME) {
            this.discard();
            return;
        }

        HitResult hitResult = this.pick();
        this.onHit(hitResult);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            if (result instanceof EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity != this.getOwner() && entity instanceof LivingEntity livingEntity) {
                    livingEntity.hurt(this.damageSources().projectile(this, (LivingEntity) this.getOwner()), damage);
                }
            }
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < range * range;
    }

    @Override
    protected float getGravity() {
        return 0.0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putFloat("Range", range);
        tag.putInt("Lifespan", lifespan);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        this.range = tag.getFloat("Range");
        this.lifespan = tag.getInt("Lifespan");
    }
}