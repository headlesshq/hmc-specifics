package me.earth.headlessmc.testmod;

import net.fabricmc.api.ClientModInitializer;

public class FabricTestMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TestMod.loaded("Fabric");
    }

}
