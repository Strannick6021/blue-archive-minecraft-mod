package com.strannick.companion.companion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;

import com.strannick.companion.entity.CompanionEntity;

public class CompanionAI {
    private final CompanionEntity companion;
    private CompanionState currentState = CompanionState.IDLE;
    private BlockPos targetBlock = null;
    private LivingEntity targetEnemy = null;
    private int actionCooldown = 0;
    private static final int SEARCH_RADIUS = 32;
    private static final int ATTACK_COOLDOWN = 20;

    public enum CompanionState {
        IDLE, FOLLOWING, MINING, DEFENDING, ATTACKING, NAVIGATING
    }

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void update() {
        if (companion.level().isClientSide) return;

        if (actionCooldown > 0) {
            actionCooldown--;
        }

        switch (currentState) {
            case IDLE:
                updateIdle();
                break;
            case FOLLOWING:
                updateFollowing();
                break;
            case MINING:
                updateMining();
                break;
            case DEFENDING:
                updateDefending();
                break;
            case ATTACKING:
                updateAttacking();
                break;
            case NAVIGATING:
                updateNavigating();
                break;
        }
    }

    private void updateIdle() {
        Player owner = companion.getOwnerPlayer();
        if (owner != null && companion.distanceTo(owner) > 20.0f) {
            currentState = CompanionState.FOLLOWING;
        }
    }

    private void updateFollowing() {
        Player owner = companion.getOwnerPlayer();
        if (owner == null) return;

        if (companion.distanceTo(owner) > 50.0f) {
            companion.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        } else if (companion.distanceTo(owner) < 2.0f) {
            companion.getNavigation().stop();
        } else {
            companion.getNavigation().moveTo(owner.getX(), owner.getY(), owner.getZ(), 1.0f);
        }

        checkForEnemies();
    }

    private void updateMining() {
        if (targetBlock == null || !isMineableBlock(companion.level().getBlockState(targetBlock).getBlock())) {
            findNearestMineableBlock();
            if (targetBlock == null) {
                currentState = CompanionState.FOLLOWING;
                companion.sayDialog(CompanionDialogs.TASK_COMPLETE);
                return;
            }
        }

        double distToBlock = companion.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distToBlock < 4.0) {
            if (actionCooldown <= 0) {
                breakBlock(targetBlock);
                actionCooldown = 5;
            }
        } else {
            navigateToBlock(targetBlock);
        }
    }

    private void updateDefending() {
        Player owner = companion.getOwnerPlayer();
        if (owner == null) {
            currentState = CompanionState.FOLLOWING;
            return;
        }

        AABB searchArea = new AABB(
                companion.getX() - SEARCH_RADIUS,
                companion.getY() - SEARCH_RADIUS,
                companion.getZ() - SEARCH_RADIUS,
                companion.getX() + SEARCH_RADIUS,
                companion.getY() + SEARCH_RADIUS,
                companion.getZ() + SEARCH_RADIUS
        );

        LivingEntity nearestEnemy = null;
        double minDistance = Double.MAX_VALUE;

        for (LivingEntity entity : companion.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity instanceof Monster && entity != companion && entity != owner) {
                double dist = companion.distanceToSqr(entity);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestEnemy = entity;
                }
            }
        }

        if (nearestEnemy != null) {
            targetEnemy = nearestEnemy;
            currentState = CompanionState.ATTACKING;
        } else {
            if (companion.distanceTo(owner) > 15.0f) {
                companion.getNavigation().moveTo(owner.getX(), owner.getY(), owner.getZ(), 1.0f);
            } else {
                companion.getNavigation().stop();
            }
        }
    }

    private void updateAttacking() {
        if (targetEnemy == null || targetEnemy.isDeadOrDying()) {
            currentState = CompanionState.DEFENDING;
            return;
        }

        Player owner = companion.getOwnerPlayer();
        if (owner != null && companion.distanceTo(owner) > SEARCH_RADIUS) {
            currentState = CompanionState.DEFENDING;
            return;
        }

        companion.getNavigation().moveTo(targetEnemy, 1.0f);

        if (companion.distanceTo(targetEnemy) < 3.0f && actionCooldown <= 0) {
            companion.doHurtTarget(targetEnemy);
            actionCooldown = ATTACK_COOLDOWN;
            companion.sayDialog(CompanionDialogs.ENEMY_NEARBY);
        }

        if (companion.distanceTo(targetEnemy) > SEARCH_RADIUS) {
            targetEnemy = null;
            currentState = CompanionState.DEFENDING;
        }
    }

    private void updateNavigating() {
        if (targetBlock == null) {
            currentState = CompanionState.IDLE;
            return;
        }

        if (companion.distanceToSqr(Vec3.atCenterOf(targetBlock)) < 4.0) {
            currentState = CompanionState.IDLE;
        }
    }

    public boolean orderMining(String blockType) {
        currentState = CompanionState.MINING;
        targetBlock = null;
        return true;
    }

    public void orderDefense() {
        currentState = CompanionState.DEFENDING;
    }

    public void orderFollow() {
        currentState = CompanionState.FOLLOWING;
        targetBlock = null;
        targetEnemy = null;
    }

    private void checkForEnemies() {
        AABB searchArea = new AABB(
                companion.getX() - 16.0,
                companion.getY() - 16.0,
                companion.getZ() - 16.0,
                companion.getX() + 16.0,
                companion.getY() + 16.0,
                companion.getZ() + 16.0
        );

        for (LivingEntity entity : companion.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity instanceof Monster) {
                targetEnemy = entity;
                currentState = CompanionState.ATTACKING;
                return;
            }
        }
    }

    private void findNearestMineableBlock() {
        BlockPos companionPos = companion.blockPosition();
        double minDistance = Double.MAX_VALUE;
        BlockPos nearest = null;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += 2) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y += 2) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z += 2) {
                    BlockPos pos = companionPos.offset(x, y, z);
                    Block block = companion.level().getBlockState(pos).getBlock();

                    if (isMineableBlock(block)) {
                        double dist = companionPos.distSqr(pos);
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }

        targetBlock = nearest;
    }

    private boolean isMineableBlock(Block block) {
        return block == Blocks.STONE || block == Blocks.DEEPSLATE || 
               block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE;
    }

    private void navigateToBlock(BlockPos target) {
        companion.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0f);
    }

    private void breakBlock(BlockPos target) {
        companion.level().destroyBlock(target, true);
        companion.sayDialog(CompanionDialogs.MINING_START);
        targetBlock = null;
    }

    public CompanionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CompanionState state) {
        this.currentState = state;
    }
}