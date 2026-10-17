package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP implements Player {
    @Shadow
    public abstract void sendChatMessage(String component);

    @Override
    public void sendMessage(String message) {
        this.sendChatMessage(message);
    }

    @Override
    public void openMenu() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiIngameMenu());
    }

    @Override
    public void openInventory() {
        if (Minecraft.getMinecraft().playerController.isRidingHorse()) {
            EntityPlayerSP.class.cast(this).sendHorseInventory();
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(EntityPlayerSP.class.cast(this)));
        }
    }

    @Override
    public void closeScreen() {
        if (Minecraft.getMinecraft().currentScreen != null) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    public boolean isDead() {
        return EntityPlayerSP.class.cast(this).getHealth() <= 0.0F;
    }

    @Override
    public void requestRespawn() {
        EntityPlayerSP.class.cast(this).respawnPlayer();
        if (Minecraft.getMinecraft().currentScreen instanceof GuiGameOver) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

}
