package net.rsm.etherealbow.item;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModSounds {
    public static final Identifier BOW_PULL_ID  = new Identifier("firstmod", "bow_pull");
    public static final SoundEvent  BOW_PULL     = SoundEvent.of(BOW_PULL_ID);

    public static final Identifier BOW_SHOOT_ID = new Identifier("firstmod", "bow_shoot");
    public static final SoundEvent  BOW_SHOOT    = SoundEvent.of(BOW_SHOOT_ID);

    /** Call this on the CLIENT to register your pull and shoot sounds. */
    public static void registerClient() {
        Registry.register(Registries.SOUND_EVENT, BOW_PULL_ID, BOW_PULL);
        Registry.register(Registries.SOUND_EVENT, BOW_SHOOT_ID, BOW_SHOOT);
        System.out.println("[ModSounds] Client sounds registered");
    }
}