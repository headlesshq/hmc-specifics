package me.earth.headlessmc.mc.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMinecraftFps {
    @Accessor("fps")
    static int getFps() {
        throw new AssertionError();
    }

}
