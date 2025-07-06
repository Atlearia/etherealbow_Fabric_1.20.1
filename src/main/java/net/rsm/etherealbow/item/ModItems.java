package net.rsm.etherealbow.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.rsm.etherealbow.FirstMod;

public class ModItems {
    public static final Item ETHEREAL_BOW = registerItem("etherealbow", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(FirstMod.MOD_ID, name), item);

    }
    public static void registerModItems()
    {
        FirstMod.LOGGER.info("Registering Mod Items for" + FirstMod.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
        entries.add(ETHEREAL_BOW);

        });
    }
}

