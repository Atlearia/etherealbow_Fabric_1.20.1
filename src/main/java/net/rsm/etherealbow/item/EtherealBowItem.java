package net.rsm.etherealbow.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.rsm.etherealbow.item.ModSounds;
import net.minecraft.sound.SoundEvents;


public class EtherealBowItem extends BowItem {
    public EtherealBowItem(Settings settings) {
        super(settings);
    }

    // 1) Play pull sound on the CLIENT as soon as right-clicking begins
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Only on the client, and only for your custom bow:
        if (world.isClient) {
            System.out.println("[EtherealBowItem] use() called, playing BOW_PULL");
            user.playSound(ModSounds.BOW_PULL, 1.0F, 1.0F);
        }

        // This starts the draw action:
        return super.use(world, user, hand);
    }



    // 2) Shoot arrow & play shoot sound on release
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;


        boolean creative = player.getAbilities().creativeMode;
        ItemStack ammo = RangedWeaponItem.getHeldProjectile(user, BOW_PROJECTILES);
        if (!creative && ammo.isEmpty()) return;

        int useTime = getMaxUseTime(stack) - remainingUseTicks;
        float pull = getPullProgress(useTime);
        if (pull < 0.1F) return;

        if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);
        ArrowItem arrowItem = (ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
        PersistentProjectileEntity arrow = arrowItem.createArrow(world, ammo, user);
        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0f, pull * 3f, 1f);

        // Apply enchantments, durability, pickup rules exactly like vanilla…
        if (pull == 1.0F) arrow.setCritical(true);
        int power = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
        if (power > 0) arrow.setDamage(arrow.getDamage() + power * .5 + .5);
        int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
        if (punch > 0) arrow.setPunch(punch);
        if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) arrow.setOnFireFor(100);

        stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
        if (!creative && arrowItem != Items.ARROW) ammo.decrement(1);

        arrow.pickupType = creative
                ? PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY
                : PersistentProjectileEntity.PickupPermission.ALLOWED;

        if (!world.isClient) {
            world.spawnEntity(arrow);
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    ModSounds.BOW_SHOOT,
                    SoundCategory.PLAYERS,
                    1.0F, 1.0F
            );
        }
    }
}
