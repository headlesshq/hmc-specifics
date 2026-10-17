package me.earth.headlessmc.mc.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {
    @Shadow
    @Final
    private EnumPacketDirection direction;

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
        at = @At("HEAD"))
    private void channelRead0Hook(ChannelHandlerContext ctx, Packet packet, CallbackInfo ci) {
        // the integrated server has NetworkManagers, too
        if (direction == EnumPacketDirection.CLIENTBOUND) {
            MetricsTracker.getInstance().onPacketReceived();
        }
    }

    @Inject(method = "dispatchPacket", at = @At("HEAD"))
    private void dispatchPacketHook(CallbackInfo ci) {
        if (direction == EnumPacketDirection.CLIENTBOUND) {
            MetricsTracker.getInstance().onPacketSent();
        }
    }

}
