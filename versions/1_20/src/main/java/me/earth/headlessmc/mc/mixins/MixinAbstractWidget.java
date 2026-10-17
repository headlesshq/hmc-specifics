package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.gui.GuiElement;
import me.earth.headlessmc.mc.IdManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget implements GuiElement {
    @Shadow
    public boolean active;
    @Shadow
    private Component message;
    @Unique
    private Integer headlessmc_id;

    @Override
    public String getText() {
        return message == null ? "" : message.getString();
    }

    @Override
    @Accessor("x")
    public abstract int getX();

    @Override
    @Accessor("y")
    public abstract int getY();

    @Override
    @Accessor("width")
    public abstract int getWidth();

    @Override
    @Accessor("height")
    public abstract int getHeight();

    @Override
    public int getId() {
        if (headlessmc_id == null) {
            this.headlessmc_id = IdManager.INSTANCE.getNextId();
        }

        return headlessmc_id;
    }

}
