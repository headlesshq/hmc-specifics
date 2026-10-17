package me.earth.headlessmc.mc.mixins;

import com.google.common.util.concurrent.ListenableFuture;
import me.earth.headlessmc.mc.FontRendererImpl;
import me.earth.headlessmc.mc.Initializer;
import me.earth.headlessmc.mc.McKeyHandler;
import me.earth.headlessmc.mc.MetricsSourceImpl;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.ScreenshotProviderImpl;
import me.earth.headlessmc.mc.auth.McAccount;
import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.metrics.MetricsTracker;
import me.earth.headlessmc.mc.player.AutoRespawn;
import me.earth.headlessmc.mc.player.Player;
import me.earth.headlessmc.mc.screenshot.ScreenshotProvider;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Future;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MixinMinecraft implements Minecraft {
    @Shadow
    public EntityPlayerSP thePlayer;
    @Shadow
    public net.minecraft.client.gui.GuiScreen currentScreen;
    @Shadow
    public WorldClient theWorld;

    @Shadow
    public abstract void shutdown();

    @Shadow
    public abstract ListenableFuture<Object> addScheduledTask(Runnable run);

    @Shadow
    public abstract void displayGuiScreen(net.minecraft.client.gui.GuiScreen guiScreenIn);

    @Shadow
    public abstract boolean isIntegratedServerRunning();

    @Shadow
    public abstract void loadWorld(WorldClient worldClientIn);

    @Shadow public abstract boolean isConnectedToRealms();

    @Mutable
    @Shadow @Final private Session session;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(GameConfiguration gameConfig, CallbackInfo ci)
        throws IOException {
        Initializer.init(this);
    }

    @Override
    public Player getPlayer() {
        return (Player) thePlayer;
    }

    @Override
    public GuiScreen getScreen() {
        return (GuiScreen) currentScreen;
    }

    @Override
    public Keyboard getKeyboard() {
        return McKeyHandler.getKeyboard();
    }

    @Override
    public void quit() {
        this.shutdown();
    }

    @Override
    public FontRenderer getFontRenderer() {
        return FontRendererImpl.INSTANCE;
    }

    @Override
    public Future<?> schedule(Runnable task) {
        return addScheduledTask(task);
    }

    @Override
    public void connect(String ip, int port) {
        this.disconnect();
        this.displayGuiScreen(new GuiConnecting(
            new GuiMainMenu(), net.minecraft.client.Minecraft.class.cast(this),
            ip, port));
    }

    @Override
    public void disconnect() {
        boolean singlePlayer = this.isIntegratedServerRunning();
        boolean realms = this.isConnectedToRealms();

        if (theWorld != null) {
            theWorld.sendQuittingDisconnectingPacket();
        }

        this.loadWorld(null);

        if (singlePlayer) {
            this.displayGuiScreen(new GuiMainMenu());
        } else if (realms) {
            new RealmsBridge().switchToRealms(new GuiMainMenu());
        } else {
            this.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
    }

    @Override
    public McAccount getMcAccount() {
        Session user = this.session;
        UUID uuid;
        try {
            uuid = UUID.fromString(user.getPlayerID());
        } catch(IllegalArgumentException e) {
            uuid = user.getProfile().getId();
        }

        return new McAccount(user.getUsername(), uuid, user.getToken());
    }

    @Override
    public void setMcAccount(McAccount account) {
        this.session = new Session(account.getName(), account.getUuid().toString(), account.getAccessToken(), "mojang");
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void tickHeadHook(CallbackInfo ci) {
        MetricsTracker.getInstance().onTickStart();
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void tickReturnHook(CallbackInfo ci) {
        MetricsTracker.getInstance().onTickEnd();
        AutoRespawn.getInstance().onTick(this);
    }

    @Override
    public MetricsSource getMetricsSource() {
        return MetricsSourceImpl.INSTANCE;
    }

    @Override
    public ScreenshotProvider getScreenshotProvider() {
        return ScreenshotProviderImpl.INSTANCE;
    }

}
