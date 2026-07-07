package com.strannick.companion.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import com.strannick.companion.registry.ModEntities;

/**
 * Снаряд дробовика - быстрая пуля с коротким временем жизни
 * Оптимизирована для эффективной работы при множественных выстрелах
 */
public class ShotgunProjectile extends Projectile {
    private static final int MAX_LIFETIME = 200; // ~10 секунд
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> DAMAGE = 
            SynchedEntityData.defineId(ShotgunProjectile.class, EntityDataSerializers.FLOAT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> RANGE = 
            SynchedEntityData.defineId(ShotgunProjectile.class, EntityDataSerializers.FLOAT);
    
    private int lifespan = 0;

    public ShotgunProjectile(EntityType<? extends ShotgunProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    public ShotgunProjectile(Level level, LivingEntity owner, float damage, float range) {
        super(ModEntities.SHOTGUN_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(RANGE, range);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DAMAGE, 12.0f);
        this.entityData.define(RANGE, 32.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // Проверяем время жизни
        lifespan++;
        if (lifespan > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // Проверяем попадание
        HitResult hitResult = this.pick();
        this.onHit(hitResult);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            if (result instanceof EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                
                // Не ударяем своего владельца
                if (entity != this.getOwner() && entity instanceof LivingEntity livingEntity) {
                    float damage = this.entityData.get(DAMAGE);
                    livingEntity.hurt(this.damageSources().projectile(this, (LivingEntity) this.getOwner()), damage);
                }
            }

            // Удаляемся после попадания
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Оптимизация: не рендерим далёкие снаряды
        float range = this.entityData.get(RANGE);
        return distance < range * range;
    }

    @Override
    protected float getGravity() {
        return 0.0f; // Нет гравитации для пуль
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.entityData.get(DAMAGE));
        tag.putFloat("Range", this.entityData.get(RANGE));
        tag.putInt("Lifespan", lifespan);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) {
            this.entityData.set(DAMAGE, tag.getFloat("Damage"));
        }
        if (tag.contains("Range")) {
            this.entityData.set(RANGE, tag.getFloat("Range"));
        }
        if (tag.contains("Lifespan")) {
            this.lifespan = tag.getInt("Lifespan");
        }
    }
}