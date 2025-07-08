package net.rsm.etherealbow.item;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;

public class EtherealBowItem extends BowItem {
    public EtherealBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);

        if (!world.isClient) {
            // Play your custom sound
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    ModSounds.BOW_SHOOT,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        // Any other custom arrow-spawn logic…
    }
}
