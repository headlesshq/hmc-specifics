package me.earth.headlessmc.mc.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@SuppressWarnings("MixinSuperClass")
@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MixinMinecraft extends MixinBlockableEventLoop implements Minecraft {
    @Shadow
    public LocalPlayer player;
    @Shadow
    public Screen screen;
    @Shadow
    public ClientLevel level;

    @Shadow
    public abstract void stop();

    @Shadow
    public abstract void setScreen(Screen screen);

    @Shadow
    public abstract boolean isLocalServer();

    @Shadow
    public abstract void clearLevel(Screen screen);

    @Shadow public abstract void clearLevel();

    @Mutable
    @Shadow @Final private User user;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(GameConfig config, CallbackInfo ci) throws IOException {
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
    }

    @Override
    public FontRenderer getFontRenderer() {
        return FontRendererImpl.INSTANCE;
    }

    @Override
    public void connect(String ip, int port) {
        this.disconnect();
        setScreen(
            new ConnectScreen(
                new TitleScreen(),
                net.minecraft.client.Minecraft.class.cast(this),
                new ServerData(
                    I18n.get("selectServer.defaultName"), ip + ":" + port, false)));
    }

    @Override
    public void disconnect() {
        ClientLevel level = this.level;
        if (level != null) {
            level.disconnect();
        }

        if (this.isLocalServer()) {
            this.clearLevel(
                new GenericDirtMessageScreen(
                    new TranslatableComponent("menu.savingLevel")));
        } else {
            this.clearLevel();
        }

        this.setScreen(new TitleScreen());
    }

    @Override
    public McAccount getMcAccount() {
        User user = this.user;
        UUID uuid;
        try {
            uuid = UUID.fromString(user.getUuid());
        } catch(IllegalArgumentException e) {
            uuid = user.getGameProfile().getId();
        }

        return new McAccount(user.getName(), uuid, user.getAccessToken());
    }

    @Override
    public void setMcAccount(McAccount account) {
        this.user = new User(account.getName(), account.getUuid().toString(), account.getAccessToken(), "mojang");
    }

    @Override
    public List<Map.Entry<String, String>> getCompletions(String line) {
        LocalPlayer player = this.player;
        //noinspection ConstantValue
        if (player != null && player.connection != null && player.connection.getCommands() != null) {
            CommandDispatcher<SharedSuggestionProvider> dispatcher = player.connection.getCommands();
            SharedSuggestionProvider suggestionsProvider = player.connection.getSuggestionsProvider();
            Collection<String> customTabSugggestions = suggestionsProvider.getOnlinePlayerNames();
            BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction = SharedSuggestionProvider::suggest;
            return BrigadierWrapper.getCompletions(dispatcher, suggestionsProvider, customTabSugggestions, suggestFunction, line);
        }

        return new ArrayList<>();
    }

}
