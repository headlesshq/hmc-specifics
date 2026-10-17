package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.metrics.MetricsSource;
import me.earth.headlessmc.mc.metrics.PlayerListEntry;
import me.earth.headlessmc.mc.metrics.PlayerPosition;
import me.earth.headlessmc.mc.mixins.IPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
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
        // debugFPS is private, Minecraft.debug is e.g. "60 fps, 3 chunk updates"
        String debug = Minecraft.getMinecraft().debug;
        int index = debug == null ? -1 : debug.indexOf(" fps");
        if (index > 0) {
            try {
                return Integer.parseInt(debug.substring(0, index).trim());
            } catch (NumberFormatException ignored) {
                // NOP
            }
        }

        return null;
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
    public Double getIntegratedServerMspt() {
        IntegratedServer server = Minecraft.getMinecraft().getIntegratedServer();
        return server == null ? null : getAverageMillis(server.tickTimeArray);
    }

    @Override
    public List<PlayerListEntry> getPlayerList() {
        List<PlayerListEntry> result = new ArrayList<>();
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getNetHandler();
        if (connection != null) {
            for (Object object : connection.playerInfoList) {
                GuiPlayerInfo info = (GuiPlayerInfo) object;
                result.add(new PlayerListEntry(info.name, null, info.responseTime, null));
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
        return world == null || world.difficultySetting == null
            ? null
            : getLastPart(world.difficultySetting.getDifficultyResourceKey());
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
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getGameProfile().getName();
    }

    @Override
    public PlayerPosition getPlayerPosition() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null
            ? null
            : new PlayerPosition(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
    }

    @Override
    public Float getHealth() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getHealth();
    }

    @Override
    public Integer getFood() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getFoodStats().getFoodLevel();
    }

    @Override
    public Float getSaturation() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.getFoodStats().getSaturationLevel();
    }

    @Override
    public Integer getXpLevel() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player == null ? null : player.experienceLevel;
    }

    @Override
    public String getGameMode() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.playerController == null ? null : ((IPlayerControllerMP) mc.playerController).getCurrentGameType().getName();
    }

    @Override
    public Boolean isDead() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
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
