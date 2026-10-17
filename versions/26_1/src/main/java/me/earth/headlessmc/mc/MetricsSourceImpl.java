package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.metrics.PlayerListEntry;
import me.earth.headlessmc.mc.metrics.PlayerPosition;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

public final class MetricsSourceImpl implements MetricsSource {
    public static final MetricsSourceImpl INSTANCE = new MetricsSourceImpl();

    private MetricsSourceImpl() {
    }

    @Override
    public String getMinecraftVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public Integer getFps() {
        return Minecraft.getInstance().getFps();
    }

    @Override
    public boolean isInWorld() {
        return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
    }

    @Override
    public boolean isSingleplayer() {
        return Minecraft.getInstance().isLocalServer();
    }

    @Override
    public String getServerAddress() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        return serverData == null ? null : serverData.ip;
    }

    @Override
    public String getServerBrand() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.serverBrand();
    }

    @Override
    public Float getTickRate() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.tickRateManager().tickrate();
    }

    @Override
    public Boolean isTickingFrozen() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.tickRateManager().isFrozen();
    }

    @Override
    public Double getIntegratedServerMspt() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        return server == null ? null : (double) server.getCurrentSmoothedTickTime();
    }

    @Override
    public Float getPacketsReceivedPerSecond() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getConnection().getAverageReceivedPackets();
    }

    @Override
    public Float getPacketsSentPerSecond() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getConnection().getAverageSentPackets();
    }

    @Override
    public List<PlayerListEntry> getPlayerList() {
        List<PlayerListEntry> result = new ArrayList<>();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            for (PlayerInfo info : connection.getListedOnlinePlayers()) {
                GameType gameType = info.getGameMode();
                result.add(new PlayerListEntry(info.getProfile().name(), info.getProfile().id(), info.getLatency(),
                                               gameType == null ? null : gameType.getName()));
            }
        }

        return result;
    }

    @Override
    public String getDimension() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : MetricsSource.getLocation(level.dimension());
    }

    @Override
    public Long getGameTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getLevelData().getGameTime();
    }

    @Override
    public Long getDayTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getOverworldClockTime();
    }

    @Override
    public Boolean isRaining() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.isRaining();
    }

    @Override
    public Boolean isThundering() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.isThundering();
    }

    @Override
    public String getDifficulty() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getLevelData().getDifficulty().getSerializedName();
    }

    @Override
    public Integer getLoadedChunks() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getChunkSource().getLoadedChunksCount();
    }

    @Override
    public Integer getEntityCount() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getEntityCount();
    }

    @Override
    public String getPlayerName() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getGameProfile().name();
    }

    @Override
    public PlayerPosition getPlayerPosition() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : new PlayerPosition(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public Float getHealth() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getHealth();
    }

    @Override
    public Integer getFood() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getFoodData().getFoodLevel();
    }

    @Override
    public Float getSaturation() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getFoodData().getSaturationLevel();
    }

    @Override
    public Integer getXpLevel() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.experienceLevel;
    }

    @Override
    public String getGameMode() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gameMode == null ? null : mc.gameMode.getPlayerMode().getName();
    }

    @Override
    public Boolean isDead() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.isDeadOrDying();
    }

}
