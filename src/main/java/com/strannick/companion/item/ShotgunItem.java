package com.strannick.companion.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;

import com.strannick.companion.registry.ModSounds;
import com.strannick.companion.entity.ShotgunProjectile;

public class ShotgunItem extends Item {
    private static final String AMMO_TAG = "Ammo";
    private static final String COOLDOWN_TAG = "Cooldown";
    private static final int MAX_AMMO = 8;
    private static final int FIRE_COOLDOWN = 20;
    private static final float DAMAGE = 12.0f;
    private static final float RANGE = 32.0f;
    private static final int PELLETS = 8;

    public ShotgunItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(1000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isShiftKeyDown()) {
            reload(itemStack, player);
            return InteractionResultHolder.success(itemStack);
        }

        if (!level.isClientSide && canShoot(itemStack, player)) {
            shoot(itemStack, level, player, hand);
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private boolean canShoot(ItemStack itemStack, Player player) {
        int ammo = getAmmo(itemStack);
        int cooldown = getCooldown(itemStack);
        return ammo > 0 && cooldown <= 0;
    }

    private void shoot(ItemStack itemStack, Level level, Player player, InteractionHand hand) {
        decreaseAmmo(itemStack, player);
        setCooldown(itemStack, FIRE_COOLDOWN);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SHOTGUN_FIRE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        Vec3 playerLook = player.getViewVector(1.0f);
        for (int i = 0; i < PELLETS; i++) {
            double spreadX = (Math.random() - 0.5) * 0.2;
            double spreadY = (Math.random() - 0.5) * 0.2;
            double spreadZ = (Math.random() - 0.5) * 0.2;
            Vec3 direction = playerLook.add(spreadX, spreadY, spreadZ).normalize();

            ShotgunProjectile projectile = new ShotgunProjectile(level, player, DAMAGE, RANGE);
            projectile.setPos(player.getEyePosition().add(direction.scale(0.5)));
            projectile.setDeltaMovement(direction.scale(2.5));
            level.addFreshEntity(projectile);
        }

        player.swing(hand, true);
    }

    private void reload(ItemStack itemStack, Player player) {
        int ammo = getAmmo(itemStack);
        if (ammo >= MAX_AMMO) return;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SHOTGUN_RELOAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        setAmmo(itemStack, MAX_AMMO);
    }

    private int getAmmo(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt(AMMO_TAG);
    }

    private void setAmmo(ItemStack itemStack, int ammo) {
        itemStack.getOrCreateTag().putInt(AMMO_TAG, Math.min(ammo, MAX_AMMO));
    }

    private void decreaseAmmo(ItemStack itemStack, Player player) {
        int ammo = getAmmo(itemStack);
        setAmmo(itemStack, ammo - 1);

        if (!player.level().isClientSide) {
            player.containerMenu.broadcastChanges();
        }
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
        }
    }
}