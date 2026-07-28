package me.earth.headlessmc.mc.mixins;

import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Bootstrap.class)
public interface IBootstrap {
    @Invoker("wrapStreams")
    static void invokeWrapStreams() {
        throw new RuntimeException("IBootstrap.invokeWrapStreams has not been mixed in!");
    }

}
