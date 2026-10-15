package me.earth.headlessmc.mc.metrics;

import java.util.Collections;
import java.util.List;

/**
 * Provides the values for metrics that need to access Minecraft, implemented by the version specific modules.
 * All methods have to be called on Minecraft's main thread.
 * Methods return {@code null} if a value is not available, e.g. because we are not ingame,
 * or because the Minecraft version does not support it.
 */
public interface MetricsSource {
    /**
     * A MetricsSource that provides no values.
     */
    MetricsSource EMPTY = () -> null;

    /**
     * @return the Minecraft version of the client, e.g. {@code 1.21.10}.
     */
    String getMinecraftVersion();

    default Integer getFps() {
        return null;
    }

    /**
     * @return {@code true} if the client is currently in a world.
     */
    default boolean isInWorld() {
        return false;
    }

    /**
     * @return {@code true} if the client is playing on its integrated server.
     */
    default boolean isSingleplayer() {
        return false;
    }

    /**
     * @return the address of the server we are connected to.
     */
    default String getServerAddress() {
        return null;
    }

    /**
     * @return the brand of the server, e.g. {@code vanilla} or {@code Paper}.
     */
    default String getServerBrand() {
        return null;
    }

    /**
     * @return the tick rate of the server, changed with the {@code /tick} command since Minecraft 1.20.3.
     */
    default Float getTickRate() {
        return null;
    }

    /**
     * @return whether the server is frozen with the {@code /tick} command since Minecraft 1.20.3.
     */
    default Boolean isTickingFrozen() {
        return null;
    }

    /**
     * @return the average milliseconds per tick of the integrated server.
     */
    default Double getIntegratedServerMspt() {
        return null;
    }

    /**
     * @return the packets received per second, if Minecraft tracks them. Otherwise, {@link MetricsTracker} is used.
     */
    default Float getPacketsReceivedPerSecond() {
        return null;
    }

    /**
     * @return the packets sent per second, if Minecraft tracks them. Otherwise, {@link MetricsTracker} is used.
     */
    default Float getPacketsSentPerSecond() {
        return null;
    }

    /**
     * @return the players in the tab list.
     */
    default List<PlayerListEntry> getPlayerList() {
        return Collections.emptyList();
    }

    /**
     * @return our own latency in milliseconds, as reported by the server in the tab list.
     */
    default Integer getPing() {
        String name = getPlayerName();
        if (name != null) {
            for (PlayerListEntry entry : getPlayerList()) {
                if (name.equals(entry.getName())) {
                    return entry.getPing();
                }
            }
        }

        return null;
    }

    default String getDimension() {
        return null;
    }

    default Long getGameTime() {
        return null;
    }

    default Long getDayTime() {
        return null;
    }

    default Boolean isRaining() {
        return null;
    }

    default Boolean isThundering() {
        return null;
    }

    default String getDifficulty() {
        return null;
    }

    /**
     * @return the amount of chunks loaded by the client.
     */
    default Integer getLoadedChunks() {
        return null;
    }

    /**
     * @return the amount of entities loaded by the client.
     */
    default Integer getEntityCount() {
        return null;
    }

    default String getPlayerName() {
        return null;
    }

    default PlayerPosition getPlayerPosition() {
        return null;
    }

    default Float getHealth() {
        return null;
    }

    default Integer getFood() {
        return null;
    }

    default Float getSaturation() {
        return null;
    }

    default Integer getXpLevel() {
        return null;
    }

    default String getGameMode() {
        return null;
    }

    default Boolean isDead() {
        return null;
    }

    /**
     * Extracts the location from the String representation of a Minecraft ResourceKey,
     * e.g. {@code minecraft:overworld} from {@code ResourceKey[minecraft:dimension / minecraft:overworld]}.
     * This way the version specific code does not have to reference ResourceLocation,
     * which has been renamed to Identifier in later versions.
     *
     * @param resourceKey the ResourceKey.
     * @return the location of the ResourceKey.
     */
    static String getLocation(Object resourceKey) {
        if (resourceKey == null) {
            return null;
        }

        String string = resourceKey.toString();
        int index = string.lastIndexOf(" / ");
        if (index >= 0 && string.endsWith("]")) {
            return string.substring(index + 3, string.length() - 1);
        }

        return string;
    }

}
