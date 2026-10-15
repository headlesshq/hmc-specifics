package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Inject(method = "handleJoinGame", at = @At("RETURN"))
    private void handleJoinGameHook(SPacketJoinGame packet, CallbackInfo ci) {
        MetricsTracker.getInstance().onJoin();
    }

    @Inject(method = "handleTimeUpdate", at = @At("RETURN"))
    private void handleTimeUpdateHook(SPacketTimeUpdate packet, CallbackInfo ci) {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world != null) {
            MetricsTracker.getInstance().onTimeUpdate(world.getTotalWorldTime());
        }
    }

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
