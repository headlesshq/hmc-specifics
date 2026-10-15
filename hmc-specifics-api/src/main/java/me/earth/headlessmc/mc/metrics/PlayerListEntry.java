package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.util.HasName;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A player in the tab list.
 */
public class PlayerListEntry implements HasName {
    private final String name;
    private final UUID uuid;
    private final int ping;
    private final String gameMode;

    /**
     * @param name the name of the player.
     * @param uuid the uuid of the player, {@code null} on versions that do not send it.
     * @param ping the latency of the player in milliseconds.
     * @param gameMode the game mode of the player, {@code null} on versions that do not send it.
     */
    public PlayerListEntry(String name, UUID uuid, int ping, String gameMode) {
        this.name = name;
        this.uuid = uuid;
        this.ping = ping;
        this.gameMode = gameMode;
    }

    @Override
    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPing() {
        return ping;
    }

    public String getGameMode() {
        return gameMode;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("uuid", uuid == null ? null : uuid.toString());
        map.put("ping", ping);
        map.put("gamemode", gameMode);
        return map;
    }

    @Override
    public String toString() {
        return "PlayerListEntry" + toMap();
    }

}
