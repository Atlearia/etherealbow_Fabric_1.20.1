package net.rsm.etherealbow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.rsm.etherealbow.item.EtherealBowItem;
import net.rsm.etherealbow.item.ModSounds;
import net.rsm.etherealbow.util.ModModelPredicates;


@Environment(EnvType.CLIENT)
public class FirstModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModModelPredicates.registerModelPredicates();
        ModSounds.registerClient();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof EtherealBowItem) {
                // Play pull sound on the client
                if (world.isClient) {
                    player.playSound(ModSounds.BOW_PULL, 1.0F, 1.0F);
                }
                // Consume the action (start using) from the client side
                return TypedActionResult.pass(stack);
            }
            // Otherwise let vanilla handle it
            return TypedActionResult.pass(stack);
        });
    }
}