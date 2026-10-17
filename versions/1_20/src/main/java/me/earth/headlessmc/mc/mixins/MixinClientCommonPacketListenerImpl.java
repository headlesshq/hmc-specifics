package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ServerResourcePacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinClientCommonPacketListenerImpl {
    @Unique
    private static final Logger HMC$LOGGER = LogManager.getLogger("HMC-Specifics");

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    private static @Nullable URL parseResourcePackUrl(String url) {
        throw new AssertionError();
    }

    // does what clicking "Yes" on the PackConfirmScreen would do, without saving the choice to the server list
    @Inject(
        method = "handleResourcePackPush",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            shift = At.Shift.AFTER),
        cancellable = true)
    private void autoAcceptResourcePackHook(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (ServerResourcePacks.isAutoAcceptEnabled()) {
            URL url = parseResourcePackUrl(packet.url());
            if (url != null) {
                HMC$LOGGER.info("Auto-accepting server resource pack {}", url);
                DownloadedPackSource packSource = minecraft.getDownloadedPackSource();
                packSource.allowServerPacks();
                packSource.pushPack(packet.id(), url, packet.hash());
                ci.cancel();
            }
        }
    }

}
