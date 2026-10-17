package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.GuiButton.class)
public abstract class MixinGuiButton implements GuiButton {
    @Override
    @Accessor("enabled")
    public abstract boolean isEnabled();

    @Override
    @Accessor("enabled")
    public abstract void setEnabled(boolean enabled);

    @Override
    @Accessor("displayString")
    public abstract String getText();

    @Override
    @Accessor("xPosition")
    public abstract int getX();

    @Override
    @Accessor("yPosition")
    public abstract int getY();

    @Override
    @Accessor("width")
    public abstract int getWidth();

    @Override
    @Accessor("height")
    public abstract int getHeight();

    @Override
    @Accessor("id")
    public abstract int getId();

}
