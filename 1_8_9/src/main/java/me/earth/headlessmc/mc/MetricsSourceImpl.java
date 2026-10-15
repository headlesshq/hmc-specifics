package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.metrics.PlayerListEntry;
import me.earth.headlessmc.mc.metrics.PlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.server.integrated.IntegratedServer;

import java.util.ArrayList;
import java.util.List;

public final class MetricsSourceImpl implements MetricsSource {
    public static final MetricsSourceImpl INSTANCE = new MetricsSourceImpl();

    private MetricsSourceImpl() {
    }

    @Override
    public String getMinecraftVersion() {
        return RealmsSharedConstants.VERSION_STRING;
    }

    @Override
    public Integer getFps() {
        return Minecraft.getDebugFPS();
    }

    @Override
    public boolean isInWorld() {
        return Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null;
    }

    @Override
    public boolean isSingleplayer() {
        return Minecraft.getMinecraft().isSingleplayer();
    }

    @Override
    public String getServerAddress() {
        ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        return serverData == null ? null : serverData.serverIP;
    }

    @Override
    public String getServerBrand() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        // the server brand, the field has been mapped incorrectly
        return player == null ? null : player.getClientBrand();
    }

    @Override
    public Double getIntegratedServerMspt() {
        IntegratedServer server = Minecraft.getMinecraft().getIntegratedServer();
        return server == null ? null : getAverageMillis(server.tickTimeArray);
    }

    @Override
    public List<PlayerListEntry> getPlayerList() {
        List<PlayerListEntry> result = new ArrayList<>();
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getNetHandler();
        if (connection != null) {
            for (NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
                result.add(new PlayerListEntry(info.getGameProfile().getName(), info.getGameProfile().getId(), info.getResponseTime(),
                                               info.getGameType() == null ? null : info.getGameType().getName()));
            }
        }

        return result;
    }

    @Override
    public String getDimension() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.provider.getDimensionName();
    }

    @Override
    public Long getGameTime() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.getTotalWorldTime();
    }

    @Override
    public Long getDayTime() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.getWorldTime();
    }

    @Override
    public Boolean isRaining() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.isRaining();
    }

    @Override
    public Boolean isThundering() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.isThundering();
    }

    @Override
    public String getDifficulty() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null || world.getDifficulty() == null
            ? null
            : getLastPart(world.getDifficulty().getDifficultyResourceKey());
    }

    @Override
    public Integer getLoadedChunks() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.getChunkProvider().getLoadedChunkCount();
    }

    @Override
    public Integer getEntityCount() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world == null ? null : world.loadedEntityList.size();
    }

    @Override
    public String getPlayerName() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getGameProfile().getName();
    }

    @Override
    public PlayerPosition getPlayerPosition() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null
            ? null
            : new PlayerPosition(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
    }

    @Override
    public Float getHealth() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getHealth();
    }

    @Override
    public Integer getFood() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getFoodStats().getFoodLevel();
    }

    @Override
    public Float getSaturation() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getFoodStats().getSaturationLevel();
    }

    @Override
    public Integer getXpLevel() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.experienceLevel;
    }

    @Override
    public String getGameMode() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.playerController == null ? null : mc.playerController.getCurrentGameType().getName();
    }

    @Override
    public Boolean isDead() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : !player.isEntityAlive();
    }

    static Double getAverageMillis(long[] tickTimes) {
        if (tickTimes == null || tickTimes.length == 0) {
            return null;
        }

        long sum = 0L;
        for (long tickTime : tickTimes) {
            sum += tickTime;
        }

        return sum / (double) tickTimes.length / 1_000_000.0;
    }

    static String getLastPart(String translationKey) {
        return translationKey == null ? null : translationKey.substring(translationKey.lastIndexOf('.') + 1);
    }

}
