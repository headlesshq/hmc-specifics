package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.gui.TextField;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends MixinAbstractWidget
    implements TextField {
    @Override
    @Invoker("getValue")
    public abstract String getText();

    @Override
    @Invoker("setValue")
    public abstract void setText(String text);

}
