package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.IdManager;
import me.earth.headlessmc.mc.gui.TextField;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField implements TextField {
    @Unique
    private Integer headlessmc_id;

    @Override
    @Invoker("getText")
    public abstract String getText();

    // TODO: this is a problem when starting the game in an IDE, because the
    //  methods deobfuscated name is setText as well we get a Stackoverflow.
    @Override
    @Invoker("setText")
    public abstract void setText(String text);

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
    public int getId() {
        if (headlessmc_id == null) {
            this.headlessmc_id = IdManager.INSTANCE.getNextId();
        }

        return headlessmc_id;
    }

}
