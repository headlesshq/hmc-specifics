package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.gui.GuiButton;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Button.class)
public abstract class MixinButton extends MixinAbstractWidget
    implements GuiButton {
    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.active = enabled;
    }

}
