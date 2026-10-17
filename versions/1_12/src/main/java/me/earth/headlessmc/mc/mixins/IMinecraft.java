package me.earth.headlessmc.mc.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMinecraft {
    @Accessor("actionKeyF3")
    boolean getActionKeyF3();

    @Accessor("actionKeyF3")
    void setActionKeyF3(boolean actionKeyF3);

}
