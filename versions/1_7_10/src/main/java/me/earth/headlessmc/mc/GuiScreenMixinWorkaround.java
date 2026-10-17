package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.mixins.IGuiContainer;
import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * Workaround because old Mixin versions, e.g. 0.7.11-SNAPSHOT,
 * which are the only ones working well on old Minecraft,
 * cannot cast to an Accessor inside a Mixin.
 */
public class GuiScreenMixinWorkaround {
    public static int[] getXYOffset(GuiContainer guiContainer) {
        /* this crashes if it is inside MixinGuiScreen
            org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException: Resolution error: unable to find corresponding type
            for me/earth/headlessmc/mc/mixins/IGuiContainer in hierarchy of net/minecraft/client/gui/GuiScreen
         */
        IGuiContainer acs = (IGuiContainer) guiContainer;
        int xOffset = (guiContainer.width - acs.getXSize()) / 2;
        int yOffset = (guiContainer.height - acs.getYSize()) / 2;
        return  new int[]{xOffset, yOffset};
    }

}
