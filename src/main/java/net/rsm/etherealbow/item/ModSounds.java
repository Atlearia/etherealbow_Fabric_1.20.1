package net.rsm.etherealbow.item;

import net.minecraft.registry.Registry;
import net.rsm.etherealbow.FirstMod;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class ModSounds {
    public static final SoundEvent BOW_SHOOT = register("bow_shoot");

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(FirstMod.MOD_ID, name);
        // SoundEvent.of(id) creates the event; Registry.register makes it available
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    // Call this once (e.g. from your main mod class) so the static initializer runs:
    public static void init() {}
}
