package me.earth.headlessmc.mc.tweaker;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

@SuppressWarnings("unused")
public class HeadlessMcMixinConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("mixins.headlessmc.json");
    }

}
