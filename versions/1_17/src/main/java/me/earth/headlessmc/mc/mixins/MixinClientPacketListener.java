package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    @Shadow
    private ClientLevel level;

    // RETURN is only reached on the main thread, the handler throws when called on the netty thread
    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void handleLoginHook(ClientboundLoginPacket packet, CallbackInfo ci) {
        MetricsTracker.getInstance().onJoin();
    }

    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void handleSetTimeHook(ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (level != null) {
            MetricsTracker.getInstance().onTimeUpdate(level.getLevelData().getGameTime());
        }
    }

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
