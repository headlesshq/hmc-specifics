package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer implements Player {
    @Shadow
    @Final
    public ClientPacketListener connection;

    @Override
    public void sendMessage(String message) {
        if (message == null || connection == null) {
            return;
        }

        String normalized = StringUtil.trimChatMessage(
            StringUtils.normalizeSpace(message.trim()));
        if (normalized.isEmpty()) {
            return;
        }

        if (normalized.startsWith("/")) {
            connection.sendCommand(normalized.substring(1));
        } else {
            connection.sendChat(normalized);
        }
    }

    @Override
    public void openMenu() {
        Minecraft.getInstance().setScreen(null);
        Minecraft.getInstance().pauseGame(false);
    }

    @Override
    public void openInventory() {
        Minecraft.getInstance().setScreen(null);
        if (Minecraft.getInstance().gameMode != null
                && Minecraft.getInstance().gameMode.isServerControlledInventory()) {
            LocalPlayer.class.cast(this).sendOpenInventory();
        } else {
            Minecraft.getInstance().setScreen(new InventoryScreen(LocalPlayer.class.cast(this)));
        }
    }

    @Override
    public void closeScreen() {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public boolean isDead() {
        return LocalPlayer.class.cast(this).isDeadOrDying();
    }

    @Override
    public void requestRespawn() {
        LocalPlayer.class.cast(this).respawn();
    }

}
