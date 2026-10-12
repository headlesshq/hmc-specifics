package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    // only affects the mode seen by handleResourcePack, so the choice is not saved to the server list
    @Redirect(
        method = "handleResourcePack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ServerData;getResourceMode()Lnet/minecraft/client/multiplayer/ServerData$ServerResourceMode;"))
    private ServerData.ServerResourceMode getResourceModeHook(ServerData serverData) {
        if (ServerResourcePacks.isAutoAcceptEnabled()) {
            return ServerData.ServerResourceMode.ENABLED;
        }

        return serverData.getResourceMode();
    }

}
