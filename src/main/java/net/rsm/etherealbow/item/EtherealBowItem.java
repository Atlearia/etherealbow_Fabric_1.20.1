package net.rsm.etherealbow.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EtherealBowItem extends BowItem {
    public EtherealBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        boolean isCreative = player.getAbilities().creativeMode;
        ItemStack ammoStack = RangedWeaponItem.getHeldProjectile(user, RangedWeaponItem.BOW_PROJECTILES);
        boolean hasAmmo = !ammoStack.isEmpty();

        if (!isCreative && !hasAmmo) return;

        int useTicks = this.getMaxUseTime(stack) - remainingUseTicks;
        float pull = BowItem.getPullProgress(useTicks);
        if (pull < 0.1F) return;

        if (!hasAmmo) {
            ammoStack = new ItemStack(Items.ARROW);
        }

        ArrowItem arrowItem = (ArrowItem) (ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
        PersistentProjectileEntity arrow = arrowItem.createArrow(world, ammoStack, user);
        arrow.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, pull * 3.0F, 1.0F);

        if (pull == 1.0F) {
            arrow.setCritical(true);
        }

        int power = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + power * 0.5 + 0.5);
        }

        int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
        if (punch > 0) {
            arrow.setPunch(punch);
        }

        if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
            arrow.setOnFireFor(100);
        }

        stack.damage(1, player, p -> p.sendToolBreakStatus(user.getActiveHand()));

        if (!isCreative && !arrowItem.equals(Items.ARROW)) {
            ammoStack.decrement(1);
        }

        arrow.pickupType = isCreative ? PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY : PersistentProjectileEntity.PickupPermission.ALLOWED;

        if (!world.isClient) {
            world.spawnEntity(arrow);
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    ModSounds.BOW_SHOOT,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }
}
