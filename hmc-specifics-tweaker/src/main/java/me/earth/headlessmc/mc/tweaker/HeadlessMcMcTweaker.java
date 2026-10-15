package me.earth.headlessmc.mc.tweaker;

import io.github.impactdevelopment.simpletweaker.SimpleTweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@SuppressWarnings("unused")
public class HeadlessMcMcTweaker extends SimpleTweaker {
    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        super.injectIntoClassLoader(classLoader);
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.headlessmc.json");
    }

}
