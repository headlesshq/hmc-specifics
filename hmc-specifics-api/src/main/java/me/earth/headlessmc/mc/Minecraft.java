package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.auth.McAccount;
import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.player.Player;
import me.earth.headlessmc.mc.scheduling.SchedulesTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the Minecraft object.
 */
public interface Minecraft extends SchedulesTasks, Adapter {
    /**
     * Returns the Minecraft player.
     *
     * @return a {@link Player} or {@code null} if we are currently not ingame.
     */
    Player getPlayer();

    /**
     * Retrieves the current {@link GuiScreen} Minecraft is displaying.
     *
     * @return the current GuiScreen or {@code null} if there's no GuiScreen.
     */
    GuiScreen getScreen();

    /**
     * Quits Minecraft, should be preferred over any {@link System#exit(int)}
     * calls to allow Minecraft to save worlds etc.
     */
    void quit();

    /**
     * @return a {@link FontRenderer} representing Minecraft's FontRenderer. The
     * FontRenderer will not be present if the game hasn't been launched with an
     * instrumentation.
     */
    FontRenderer getFontRenderer();

    Keyboard getKeyboard();

    /**
     * Connects you to the server with the given Ip and port.
     *
     * @param ip the ip of the server to connect to.
     * @param port the port of the server to connect to.
     */
    void connect(String ip, int port);

    /**
     * Disconnects you if you are connected to a server.
     */
    void disconnect();

    /**
     * Retrieves the account that is used to authenticate with servers.
     *
     * @return the current session of Minecraft. Nullable.
     */
    McAccount getMcAccount();

    /**
     * Sets the account that is used to authenticate with servers.
     *
     * @param account the account to authenticate with.
     */
    void setMcAccount(McAccount account);

    /**
     * Gets completions for a command.
     *
     * @param line the command to complete.
     * @see "me.earth.headlessmc.mc.brigadier.BrigadierSuggestionsProvider in hmc-specifics-brigadier"
     * @return the completions for the given command.
     */
    default List<Map.Entry<String, String>> getCompletions(String line) {
        return new ArrayList<>();
    }

    /**
     * Provides the version specific values for the metrics of the {@link me.earth.headlessmc.mc.metrics.MetricsService}.
     *
     * @return the MetricsSource for this Minecraft version.
     */
    default MetricsSource getMetricsSource() {
        return MetricsSource.EMPTY;
    }

}
