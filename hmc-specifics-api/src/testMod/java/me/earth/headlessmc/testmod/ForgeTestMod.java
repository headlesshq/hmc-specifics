package me.earth.headlessmc.testmod;

import net.minecraftforge.fml.common.Mod;

/**
 * Forge 1.13+ reads {@code value}, Forge 1.8.9 - 1.12.2 reads {@code modid}.
 */
@Mod(value = TestMod.ID, modid = TestMod.ID, name = TestMod.NAME, version = TestMod.VERSION)
public class ForgeTestMod {
    public ForgeTestMod() {
        TestMod.loaded("Forge");
    }

}
