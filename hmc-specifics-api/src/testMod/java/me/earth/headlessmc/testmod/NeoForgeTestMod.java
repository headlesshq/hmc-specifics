package me.earth.headlessmc.testmod;

import net.neoforged.fml.common.Mod;

@Mod(TestMod.ID)
public class NeoForgeTestMod {
    public NeoForgeTestMod() {
        TestMod.loaded("NeoForge");
    }

}
