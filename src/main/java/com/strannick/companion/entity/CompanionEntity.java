package com.strannick.companion.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import com.strannick.companion.registry.ModSounds;
import com.strannick.companion.companion.CompanionAI;
import com.strannick.companion.companion.CompanionDialogs;

import java.util.UUID;

public class CompanionEntity extends Mob {
    private static final float FOLLOW_DISTANCE = 16.0f;
    private static final float ATTACK_RANGE = 2.5f;
    private static final int AI_UPDATE_INTERVAL = 20;

    private CompanionAI companionAI;
    private int aiUpdateCounter = 0;
    private UUID ownerUUID = null;
    private String companionName = "Компаньон";

    public CompanionEntity(EntityType<? extends CompanionEntity> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_DISTANCE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.companionAI = new CompanionAI(this);
    }

    public static boolean checkCompanionSpawnRules(EntityType<CompanionEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockState blockState, BlockPos pos) {
        return level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            aiUpdateCounter++;
            if (aiUpdateCounter >= AI_UPDATE_INTERVAL) {
                aiUpdateCounter = 0;
                if (companionAI != null) {
                    companionAI.update();
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putString("CompanionName", companionName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("CompanionName")) {
            this.companionName = tag.getString("CompanionName");
        }
    }

    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
    }

    public Player getOwnerPlayer() {
        if (ownerUUID == null) return null;
        return this.level().getPlayerByUUID(ownerUUID);
    }

    public void setCompanionName(String name) {
        this.companionName = name;
    }

    public String getCompanionName() {
        return companionName;
    }

    public void orderMining(String targetBlockType) {
        if (companionAI != null) {
            boolean success = companionAI.orderMining(targetBlockType);
            if (success) {
                sayDialog(CompanionDialogs.MINING_START);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.COMPANION_AFFIRM.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            } else {
                sayDialog(CompanionDialogs.WRONG_TOOL);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.COMPANION_DENY.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
    }

    public void orderDefense() {
        if (companionAI != null) {
            companionAI.orderDefense();
            sayDialog(CompanionDialogs.DEFENSE_START);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.COMPANION_AFFIRM.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    public void orderFollow() {
        if (companionAI != null) {
            companionAI.orderFollow();
            sayDialog(CompanionDialogs.FOLLOW_START);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.COMPANION_AFFIRM.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    public void sayDialog(String[] dialogs) {
        if (dialogs != null && dialogs.length > 0) {
            String selectedDialog = dialogs[(int) (Math.random() * dialogs.length)];
            this.displayCustomName(Component.literal("§d" + selectedDialog + "§r"));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    public CompanionAI getCompanionAI() {
        return companionAI;
    }
}