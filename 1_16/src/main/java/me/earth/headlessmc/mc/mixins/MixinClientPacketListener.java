package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    // only affects the status seen by handleResourcePack, so the choice is not saved to the server list
    @Redirect(
        method = "handleResourcePack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ServerData;getResourcePackStatus()Lnet/minecraft/client/multiplayer/ServerData$ServerPackStatus;"))
    private ServerData.ServerPackStatus getResourcePackStatusHook(ServerData serverData) {
        if (ServerResourcePacks.isAutoAcceptEnabled()) {
            return ServerData.ServerPackStatus.ENABLED;
        }

        return serverData.getResourcePackStatus();
    }

}
