package me.earth.headlessmc.mc.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinConnection {
    @Shadow
    @Final
    private PacketFlow receiving;

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"))
    private void channelRead0Hook(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        // the integrated server has connections, too
        if (receiving == PacketFlow.CLIENTBOUND) {
            MetricsTracker.getInstance().onPacketReceived();
        }
    }

}
