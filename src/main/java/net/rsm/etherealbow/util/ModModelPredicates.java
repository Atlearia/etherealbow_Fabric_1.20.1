package net.rsm.etherealbow.util;


import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.rsm.etherealbow.item.ModItems;

public class ModModelPredicates {
    public static void registerModelPredicates() {
        registerCustomBow(ModItems.ETHEREAL_BOW);

    }




    private static void registerCustomBow(Item item) {
        ModelPredicateProviderRegistry.register(item, new Identifier("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItem() != stack ? 0.0F : (float)(stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20.0F;
            }
        });
        ModelPredicateProviderRegistry.register(
                item,
                new Identifier("pulling"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
        );
    }

}
