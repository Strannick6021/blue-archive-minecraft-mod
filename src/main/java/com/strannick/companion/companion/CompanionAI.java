package com.strannick.companion.companion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.strannick.companion.entity.CompanionEntity;
import com.strannick.companion.config.CompanionConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * AI система компаньона с оптимизированной навигацией
 * Кеширует результаты для минимальной нагрузки на сервер
 */
public class CompanionAI {
    private final CompanionEntity companion;
    private CompanionState currentState = CompanionState.IDLE;
    private BlockPos targetBlock = null;
    private LivingEntity targetEnemy = null;
    private int actionCooldown = 0;
    private int searchCooldown = 0; // Кеш для поиска
    private List<BlockPos> cachedMineableBlocks = new ArrayList<>();
    
    private static final int SEARCH_RADIUS = 32;
    private static final int ATTACK_COOLDOWN = 20;
    private static final int SEARCH_CACHE_TIME = 100; // Кеш на 5 секунд (100 тиков / 20)

    public enum CompanionState {
        IDLE, FOLLOWING, MINING, DEFENDING, ATTACKING, NAVIGATING
    }

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void update() {
        if (companion.level().isClientSide) return;

        // Обновляем кулдауны
        if (actionCooldown > 0) {
            actionCooldown--;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
        }

        // Основной цикл AI
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

        // Если далеко - телепортируемся
        if (companion.distanceTo(owner) > 50.0f) {
            companion.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
        // Если близко - просто стоим
        else if (companion.distanceTo(owner) < 2.0f) {
            companion.getNavigation().stop();
        }
        // Иначе идём к владельцу
        else {
            companion.getNavigation().moveTo(owner.getX(), owner.getY(), owner.getZ(), 1.0f);
        }

        // Проверяем врагов - если вражеские мобы близко, переходим в режим защиты
        checkForEnemies();
    }

    private void updateMining() {
        // Если нет целевого блока - ищем новый
        if (targetBlock == null || !isMineableBlock(companion.level().getBlockState(targetBlock).getBlock())) {
            if (searchCooldown <= 0) {
                findNearestMineableBlock();
                searchCooldown = SEARCH_CACHE_TIME;
            }
            
            if (targetBlock == null) {
                currentState = CompanionState.FOLLOWING;
                companion.sayDialog(CompanionDialogs.TASK_COMPLETE);
                return;
            }
        }

        // Проверяем расстояние до блока
        double distToBlock = companion.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distToBlock < 4.0) {
            // Добываем блок
            if (actionCooldown <= 0) {
                breakBlock(targetBlock);
                actionCooldown = 5;
            }
        } else {
            // Идём к блоку
            navigateToBlock(targetBlock);
        }
    }

    private void updateDefending() {
        Player owner = companion.getOwnerPlayer();
        if (owner == null) {
            currentState = CompanionState.FOLLOWING;
            return;
        }

        // Проверяем врагов в радиусе - с оптимизацией
        if (searchCooldown <= 0) {
            checkForEnemiesOptimized();
            searchCooldown = SEARCH_CACHE_TIME / 2; // Проверяем врагов чаще
        }

        if (targetEnemy != null && !targetEnemy.isDeadOrDying()) {
            currentState = CompanionState.ATTACKING;
        } else {
            // Остаёмся рядом с владельцем в режиме защиты
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

        // Идём к врагу
        companion.getNavigation().moveTo(targetEnemy, 1.0f);

        // Атакуем, если близко
        if (companion.distanceTo(targetEnemy) < 3.0f && actionCooldown <= 0) {
            companion.doHurtTarget(targetEnemy);
            actionCooldown = ATTACK_COOLDOWN;
            companion.sayDialog(CompanionDialogs.ENEMY_NEARBY);
        }

        // Если враг далеко и мы его потеряли - переходим обратно в защиту
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

    // ===== Приказы =====

    public boolean orderMining(String blockType) {
        currentState = CompanionState.MINING;
        targetBlock = null;
        searchCooldown = 0; // Сразу ищем блоки
        return true;
    }

    public void orderDefense() {
        currentState = CompanionState.DEFENDING;
        searchCooldown = 0;
    }

    public void orderFollow() {
        currentState = CompanionState.FOLLOWING;
        targetBlock = null;
        targetEnemy = null;
    }

    // ===== Вспомогательные методы (оптимизированные) =====

    private void checkForEnemiesOptimized() {
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
                return;
            }
        }
    }

    private void findNearestMineableBlock() {
        BlockPos companionPos = companion.blockPosition();
        double minDistance = Double.MAX_VALUE;
        BlockPos nearest = null;

        // Оптимизированный поиск: проверяем блоки с шагом 2
        // Это снижает количество проверок с ~262k до ~32k
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
        targetBlock = null;
    }

    public CompanionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CompanionState state) {
        this.currentState = state;
    }
}