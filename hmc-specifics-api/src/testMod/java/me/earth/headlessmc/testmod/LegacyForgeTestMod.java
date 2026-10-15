package me.earth.headlessmc.testmod;

import cpw.mods.fml.common.Mod;

/**
 * For Forge 1.7.10.
 */
@Mod(modid = TestMod.ID, name = TestMod.NAME, version = TestMod.VERSION)
public class LegacyForgeTestMod {
    public LegacyForgeTestMod() {
        TestMod.loaded("Forge 1.7.10");
    }

}
