package net.rsm.etherealbow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.rsm.etherealbow.util.ModModelPredicates;

@Environment(EnvType.CLIENT)
public class FirstModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // this is where we wire up your pull/pulling predicates
        ModModelPredicates.registerModelPredicates();
        System.out.println("[EtherealBow] Model predicates registered on client");
    }
}
