package me.earth.headlessmc.mc.mixins;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import me.earth.headlessmc.mc.FontRendererImpl;
import me.earth.headlessmc.mc.Initializer;
import me.earth.headlessmc.mc.McKeyHandler;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.auth.McAccount;
import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.UUID;
import java.util.concurrent.Future;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MixinMinecraft implements Minecraft {
    @Shadow
    public EntityClientPlayerMP thePlayer;
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

    @Mutable
    @Shadow @Final private Session session;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Session i, int j, int bl, boolean bl2, boolean file, File file2, File file3, File proxy, Proxy string, String multimap, Multimap string2, String par12, CallbackInfo ci) throws IOException {
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
        GuiMainMenu menu = new GuiMainMenu();
        // On Forge, route the join through FMLClientHandler.connectToServer: it shows the same
        // connecting screen AND initializes FMLClientHandler.playClientBlock - the latch the FML
        // mod-list handshake awaits after login. A hand-built GuiConnecting leaves that latch null,
        // so the handshake NPEs in waitForPlayClient() right after S02PacketLoginSuccess. Forge is
        // not on this shared source set's compile classpath (only the vanilla client is), so call it
        // reflectively; fall back to the vanilla connect when off Forge (e.g. Fabric).
        try {
            Class<?> fml = Class.forName("cpw.mods.fml.client.FMLClientHandler");
            Class<?> guiScreen = Class.forName("net.minecraft.client.gui.GuiScreen");
            Object handler = fml.getMethod("instance").invoke(null);
            fml.getMethod("connectToServer", guiScreen, ServerData.class)
               .invoke(handler, menu, new ServerData("headlessmc", ip + ":" + port));
        } catch (Throwable t) {
            this.displayGuiScreen(new GuiConnecting(
                menu, net.minecraft.client.Minecraft.class.cast(this), ip, port));
        }
    }

    @Override
    public void disconnect() {
        boolean singlePlayer = this.isIntegratedServerRunning();
        // lets ignore realms on 1.7!
        if (theWorld != null) {
            theWorld.sendQuittingDisconnectingPacket();
        }

        try {
            this.loadWorld(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (singlePlayer) {
            this.displayGuiScreen(new GuiMainMenu());
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

}
