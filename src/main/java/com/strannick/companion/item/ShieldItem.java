package com.strannick.companion.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class ShieldItem extends Item {
    private static final String ACTIVE_TAG = "Active";
    private static final String COOLDOWN_TAG = "Cooldown";
    private static final int BLOCK_COOLDOWN = 5;
    private static final float DAMAGE_REDUCTION = 0.7f;

    public ShieldItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(500));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            activateShield(itemStack, player);
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private void activateShield(ItemStack itemStack, Player player) {
        int cooldown = getCooldown(itemStack);
        if (cooldown > 0) return;

        setActive(itemStack, true);
        setCooldown(itemStack, BLOCK_COOLDOWN);
        player.swing(player.getUsedItemHand(), true);
    }

    private boolean isActive(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getBoolean(ACTIVE_TAG);
    }

    private void setActive(ItemStack itemStack, boolean active) {
        itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
    }

    private int getCooldown(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt(COOLDOWN_TAG);
    }

    private void setCooldown(ItemStack itemStack, int cooldown) {
        itemStack.getOrCreateTag().putInt(COOLDOWN_TAG, cooldown);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, LivingEntity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            int cooldown = getCooldown(itemStack);
            if (cooldown > 0) {
                setCooldown(itemStack, cooldown - 1);
            }
            
            if (isActive(itemStack) && cooldown <= 0) {
                setActive(itemStack, false);
            }
        }
    }

    public static float getBlockedDamage(float damage) {
        return damage * (1.0f - DAMAGE_REDUCTION);
    }
}