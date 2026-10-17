package me.earth.headlessmc.mc.mixins;

import net.minecraft.init.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Bootstrap.class)
public interface IBootstrap {
    @Invoker("redirectOutputToLog")
    static void invokeRedirectOutputToLog() {
        throw new RuntimeException("IBootstrap.redirectOutputToLog has not been mixed in!");
    }

}
