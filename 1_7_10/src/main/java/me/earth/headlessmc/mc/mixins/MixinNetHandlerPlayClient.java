package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData$ServerResourceMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Inject(method = "handleJoinGame", at = @At("RETURN"))
    private void handleJoinGameHook(S01PacketJoinGame packet, CallbackInfo ci) {
        MetricsTracker.getInstance().onJoin();
    }

    @Inject(method = "handleTimeUpdate", at = @At("RETURN"))
    private void handleTimeUpdateHook(S03PacketTimeUpdate packet, CallbackInfo ci) {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        if (world != null) {
            MetricsTracker.getInstance().onTimeUpdate(world.getTotalWorldTime());
        }
    }

    // only affects the mode seen by handleCustomPayload, so the choice is not saved to the server list
    @Redirect(
        method = "handleCustomPayload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ServerData;getResourceMode()Lnet/minecraft/client/multiplayer/ServerData$ServerResourceMode;"))
    private ServerData$ServerResourceMode getResourceModeHook(ServerData serverData) {
        if (ServerResourcePacks.isAutoAcceptEnabled()) {
            return ServerData$ServerResourceMode.ENABLED;
        }

        return serverData.getResourceMode();
    }

}
