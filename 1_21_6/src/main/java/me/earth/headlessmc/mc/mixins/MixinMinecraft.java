package me.earth.headlessmc.mc.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.realmsclient.RealmsMainScreen;
import me.earth.headlessmc.mc.FontRendererImpl;
import me.earth.headlessmc.mc.Initializer;
import me.earth.headlessmc.mc.KeyboardImpl;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.auth.McAccount;
import me.earth.headlessmc.mc.brigadier.BrigadierWrapper;
import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.player.Player;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MixinMinecraft extends MixinBlockableEventLoop
    implements Minecraft {
    @Shadow
    public LocalPlayer player;
    @Shadow
    public Screen screen;
    @Shadow
    public ClientLevel level;
    @Shadow
    @Final
    @Mutable
    private User user;

    @Shadow
    public abstract void stop();
    @Shadow
    public abstract void setScreen(Screen arg);
    @Shadow
    public abstract boolean isLocalServer();
    @Shadow
    public abstract void clearClientLevel(Screen arg);

    @Shadow public abstract void disconnectWithSavingScreen();

    @Shadow public abstract @Nullable ServerData getCurrentServer();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(GameConfig config, CallbackInfo ci) {
        Initializer.init(this);
    }

    @Override
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public GuiScreen getScreen() {
        return (GuiScreen) screen;
    }

    @Override
    public Keyboard getKeyboard() {
        return new KeyboardImpl(net.minecraft.client.Minecraft.class.cast(this));
    }

    @Override
    public void quit() {
        this.stop();
        // TODO: has been removed in an earlier version of hmc-specifics?
        boolean localServer = isLocalServer();
        ServerData serverData = getCurrentServer();
        ClientLevel level = this.level;
        if (level != null) {
            level.disconnect(Component.literal("Quitting"));
        }

        if (localServer) {
            disconnectWithSavingScreen();
        } else {
            disconnect();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (localServer) {
            setScreen(titleScreen);
        } else if (serverData != null && serverData.isRealm()) {
            setScreen(new RealmsMainScreen(titleScreen));
        } else {
            setScreen(new JoinMultiplayerScreen(titleScreen));
        }
    }

    @Override
    public FontRenderer getFontRenderer() {
        return FontRendererImpl.INSTANCE;
    }

    @Override
    public void connect(String ip, int port) {
        this.disconnect();
        ServerAddress address = new ServerAddress(ip, port);
        ConnectScreen.startConnecting(
            new TitleScreen(), net.minecraft.client.Minecraft.class.cast(this),
            address, new ServerData(I18n.get("selectServer.defaultName"),
                                    address.toString(), ServerData.Type.OTHER), false, null);
    }

    @Override
    public void disconnect() {
        ClientLevel level = this.level;
        if (level != null) {
            level.disconnect(Component.literal("Disconnecting"));
        }

        if (this.isLocalServer()) {
            this.clearClientLevel(new GenericMessageScreen(
                Component.translatable("menu.savingLevel")));
        } else {
            this.clearClientLevel(new GenericMessageScreen(
                Component.translatable("multiplayer.disconnect.generic")));
        }

        this.setScreen(new TitleScreen());
    }

    @Override
    public McAccount getMcAccount() {
        User user = this.user;
        return user == null ? null : new McAccount(user.getName(), user.getProfileId(), user.getAccessToken());
    }

    @Override
    public void setMcAccount(McAccount account) {
        this.user = new User(account.getName(), account.getUuid(), account.getAccessToken(), account.getXuid(), account.getClientId(), User.Type.MSA);
    }

    @Override
    public List<Map.Entry<String, String>> getCompletions(String line) {
        LocalPlayer player = this.player;
        //noinspection ConstantValue
        if (player != null && player.connection != null && player.connection.getCommands() != null) {
            CommandDispatcher<ClientSuggestionProvider> dispatcher = player.connection.getCommands();
            ClientSuggestionProvider suggestionsProvider = player.connection.getSuggestionsProvider();
            Collection<String> customTabSugggestions = suggestionsProvider.getCustomTabSugggestions();
            BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction = SharedSuggestionProvider::suggest;
            return BrigadierWrapper.getCompletions(dispatcher, suggestionsProvider, customTabSugggestions, suggestFunction, line);
        }

        return new ArrayList<>();
    }

}
