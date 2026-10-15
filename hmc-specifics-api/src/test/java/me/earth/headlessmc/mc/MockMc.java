package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.auth.McAccount;
import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.player.Player;
import me.earth.headlessmc.mc.screenshot.ScreenshotProvider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public enum MockMc implements Minecraft {
    INSTANCE;

    public String ip;
    public int port = -1;
    public McAccount account;
    public MetricsSource metricsSource = MetricsSource.EMPTY;
    public Player player;
    public ScreenshotProvider screenshotProvider = ScreenshotProvider.UNSUPPORTED;

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public GuiScreen getScreen() {
        return null;
    }

    @Override
    public void quit() {

    }

    @Override
    public FontRenderer getFontRenderer() {
        return null;
    }

    @Override
    public Keyboard getKeyboard() {
        return null;
    }

    @Override
    public void connect(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void disconnect() {
        this.ip = null;
        this.port = -1;
    }

    @Override
    public McAccount getMcAccount() {
        return account;
    }

    @Override
    public void setMcAccount(McAccount account) {
        this.account = account;
    }

    @Override
    public MetricsSource getMetricsSource() {
        return metricsSource;
    }

    @Override
    public ScreenshotProvider getScreenshotProvider() {
        return screenshotProvider;
    }

    @Override
    public Future<?> schedule(Runnable task) {
        task.run();
        return CompletableFuture.completedFuture(null);
    }

}
