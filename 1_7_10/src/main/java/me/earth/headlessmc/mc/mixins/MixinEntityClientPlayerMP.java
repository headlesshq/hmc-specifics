package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus$EnumState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityClientPlayerMP.class)
public abstract class MixinEntityClientPlayerMP implements Player {
    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;

    @Override
    public void sendMessage(String message) {
        this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    @Override
    public void openMenu() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiIngameMenu());
    }

    @Override
    public void openInventory() {
        if (Minecraft.getMinecraft().playerController.isRidingHorse()) {
            Minecraft.getMinecraft().thePlayer.sendHorseInteraction();
        } else {
            // Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus$EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().thePlayer));
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
        return EntityClientPlayerMP.class.cast(this).getHealth() <= 0.0F;
    }

    @Override
    public void requestRespawn() {
        EntityClientPlayerMP.class.cast(this).respawnPlayer();
        if (Minecraft.getMinecraft().currentScreen instanceof GuiGameOver) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

}
