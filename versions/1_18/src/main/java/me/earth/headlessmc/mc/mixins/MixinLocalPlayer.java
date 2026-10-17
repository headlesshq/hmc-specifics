package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer implements Player {
    @Shadow
    public abstract void chat(String component);

    @Override
    public void sendMessage(String message) {
        this.chat(message);
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
        if (Minecraft.getInstance().screen instanceof DeathScreen) {
            Minecraft.getInstance().setScreen(null);
        }
    }

}
