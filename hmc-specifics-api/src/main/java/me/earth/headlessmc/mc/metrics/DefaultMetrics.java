package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.Minecraft;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The metrics registered by HMC-Specifics.
 */
public final class DefaultMetrics {
    private static final long MB = 1024L * 1024L;

    private DefaultMetrics() {
        throw new AssertionError();
    }

    public static void registerAll(MetricsService service, Minecraft mc, MetricsTracker tracker) {
        registerJvm(service);
        MetricsSource source = mc.getMetricsSource();
        registerClient(service, source, tracker);
        registerServer(service, source, tracker);
        registerPlayers(service, source);
        registerWorld(service, source);
        registerPlayer(service, source);
    }

    public static void registerJvm(MetricsService service) {
        service.register(Metric.of("jvm.memory", "Heap memory used, free, total and max in MB.", () -> {
            Runtime runtime = Runtime.getRuntime();
            long total = runtime.totalMemory();
            long free = runtime.freeMemory();
            long max = runtime.maxMemory();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("used", (total - free) / MB);
            map.put("free", free / MB);
            map.put("total", total / MB);
            map.put("max", max / MB);
            map.put("percent", (total - free) * 100.0 / max);
            return map;
        }));
        service.register(Metric.of("jvm.gc", "Collections and total collection time in ms per garbage collector.", () -> {
            List<Map<String, Object>> result = new ArrayList<>();
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", gc.getName());
                map.put("count", gc.getCollectionCount());
                map.put("time", gc.getCollectionTime());
                result.add(map);
            }

            return result;
        }));
        service.register(Metric.of("jvm.threads", "Live, peak and daemon threads.", () -> {
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("live", threads.getThreadCount());
            map.put("peak", threads.getPeakThreadCount());
            map.put("daemon", threads.getDaemonThreadCount());
            return map;
        }));
        service.register(Metric.of("jvm.cpu", "CPU load of this process and of the system in percent.", () -> {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("process", getCpuLoad(os, "getProcessCpuLoad"));
            map.put("system", getCpuLoad(os, "getSystemCpuLoad"));
            return map;
        }));
        service.register(Metric.of("jvm.uptime", "Uptime of the JVM in seconds.",
            () -> TimeUnit.MILLISECONDS.toSeconds(ManagementFactory.getRuntimeMXBean().getUptime())));
        service.register(Metric.of("jvm.processors", "Processors available to the JVM.",
            () -> Runtime.getRuntime().availableProcessors()));
    }

    public static void registerClient(MetricsService service, MetricsSource source, MetricsTracker tracker) {
        service.register(Metric.of("client.version", "The Minecraft version of the client.", source::getMinecraftVersion));
        service.register(Metric.of("client.fps", "Frames per second.", source::getFps));
        service.register(Metric.of("client.mspt", "Average and max milliseconds per client tick over the last 100 ticks.", () -> {
            Double average = tracker.getAverageTickTime();
            if (average == null) {
                return null;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("avg", average);
            map.put("max", tracker.getMaxTickTime());
            return map;
        }));
    }

    public static void registerServer(MetricsService service, MetricsSource source, MetricsTracker tracker) {
        service.register(Metric.of("server.connected", "Whether the client is in a world.", source::isInWorld));
        service.register(Metric.of("server.address", "The address of the server.", inWorld(source, MetricsSource::getServerAddress)));
        service.register(Metric.of("server.singleplayer", "Whether the client plays on its integrated server.",
            inWorld(source, MetricsSource::isSingleplayer)));
        service.register(Metric.of("server.brand", "The brand of the server.", inWorld(source, MetricsSource::getServerBrand)));
        service.register(Metric.of("server.ping", "Our latency in ms, as reported by the server.", inWorld(source, MetricsSource::getPing)));
        service.register(Metric.of("server.tps", "Server TPS, estimated from the game time the server sends every second.",
            inWorld(source, s -> tracker.getTps(s.getTickRate()))));
        service.register(Metric.of("server.tickrate", "Tick rate of the server and if it is frozen, since 1.20.3.", inWorld(source, s -> {
            Float tickRate = s.getTickRate();
            if (tickRate == null) {
                return null;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("rate", tickRate);
            map.put("frozen", s.isTickingFrozen());
            return map;
        })));
        service.register(Metric.of("server.integrated", "TPS and milliseconds per tick of the integrated server.", inWorld(source, s -> {
            Double mspt = s.getIntegratedServerMspt();
            if (mspt == null) {
                return null;
            }

            Float tickRate = s.getTickRate();
            double maxTps = tickRate == null ? 20.0 : tickRate;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("tps", mspt <= 0.0 ? maxTps : Math.min(maxTps, 1000.0 / mspt));
            map.put("mspt", mspt);
            return map;
        })));
        service.register(Metric.of("server.packets", "Packets received and sent per second.", inWorld(source, s -> {
            Float in = s.getPacketsReceivedPerSecond();
            Float out = s.getPacketsSentPerSecond();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("in", in == null ? tracker.getPacketsReceivedPerSecond() : in);
            map.put("out", out == null ? tracker.getPacketsSentPerSecond() : out);
            return map;
        })));
        service.register(Metric.of("server.lastpacket", "Milliseconds since the last packet has been received.",
            inWorld(source, s -> tracker.getMillisSinceLastPacket())));
        service.register(Metric.of("server.session", "Seconds since joining the world.", inWorld(source, s -> tracker.getSecondsSinceJoin())));
    }

    public static void registerPlayers(MetricsService service, MetricsSource source) {
        service.register(Metric.of("players.count", "Amount of players in the tab list.", inWorld(source, s -> s.getPlayerList().size())));
        service.register(Metric.of("players.list", "Name, uuid, ping and game mode of the players in the tab list.", inWorld(source, s -> {
            List<Map<String, Object>> result = new ArrayList<>();
            for (PlayerListEntry entry : s.getPlayerList()) {
                result.add(entry.toMap());
            }

            return result;
        })));
        service.register(Metric.of("players.ping", "Min, average and max ping of the players in the tab list.", inWorld(source, s -> {
            List<PlayerListEntry> players = s.getPlayerList();
            if (players.isEmpty()) {
                return null;
            }

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            long sum = 0;
            for (PlayerListEntry entry : players) {
                min = Math.min(min, entry.getPing());
                max = Math.max(max, entry.getPing());
                sum += entry.getPing();
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("min", min);
            map.put("avg", sum / (double) players.size());
            map.put("max", max);
            return map;
        })));
    }

    public static void registerWorld(MetricsService service, MetricsSource source) {
        service.register(Metric.of("world.dimension", "The dimension the player is in.", inWorld(source, MetricsSource::getDimension)));
        service.register(Metric.of("world.time", "Game time and day time of the world.", inWorld(source, s -> {
            Long gameTime = s.getGameTime();
            if (gameTime == null) {
                return null;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("game", gameTime);
            map.put("day", s.getDayTime());
            return map;
        })));
        service.register(Metric.of("world.weather", "The weather, clear, rain or thunder.", inWorld(source, s -> {
            Boolean raining = s.isRaining();
            if (raining == null) {
                return null;
            }

            return Boolean.TRUE.equals(s.isThundering()) ? "thunder" : raining ? "rain" : "clear";
        })));
        service.register(Metric.of("world.difficulty", "The difficulty of the world.", inWorld(source, MetricsSource::getDifficulty)));
        service.register(Metric.of("world.chunks", "Amount of chunks loaded by the client.", inWorld(source, MetricsSource::getLoadedChunks)));
        service.register(Metric.of("world.entities", "Amount of entities loaded by the client.", inWorld(source, MetricsSource::getEntityCount)));
    }

    public static void registerPlayer(MetricsService service, MetricsSource source) {
        service.register(Metric.of("player.name", "The name of the player.", inWorld(source, MetricsSource::getPlayerName)));
        service.register(Metric.of("player.position", "Position and rotation of the player.", inWorld(source, s -> {
            PlayerPosition position = s.getPlayerPosition();
            return position == null ? null : position.toMap();
        })));
        service.register(Metric.of("player.health", "Health of the player.", inWorld(source, MetricsSource::getHealth)));
        service.register(Metric.of("player.food", "Food level and saturation of the player.", inWorld(source, s -> {
            Integer food = s.getFood();
            if (food == null) {
                return null;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("food", food);
            map.put("saturation", s.getSaturation());
            return map;
        })));
        service.register(Metric.of("player.xp", "Experience level of the player.", inWorld(source, MetricsSource::getXpLevel)));
        service.register(Metric.of("player.gamemode", "Game mode of the player.", inWorld(source, MetricsSource::getGameMode)));
        service.register(Metric.of("player.dead", "Whether the player is dead.", inWorld(source, MetricsSource::isDead)));
    }

    private static <T> Supplier<T> inWorld(MetricsSource source, Function<MetricsSource, T> function) {
        return () -> source.isInWorld() ? function.apply(source) : null;
    }

    /**
     * {@code com.sun.management.OperatingSystemMXBean} is not available on every JVM, so we use reflection.
     */
    private static Double getCpuLoad(OperatingSystemMXBean os, String methodName) {
        try {
            // invoke through the exported interface, the implementation class is not accessible on Java 9+
            Class<?> type = Class.forName("com.sun.management.OperatingSystemMXBean");
            if (!type.isInstance(os)) {
                return null;
            }

            Method method = type.getMethod(methodName);
            double load = ((Number) method.invoke(os)).doubleValue();
            return load < 0.0 ? null : load * 100.0;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

}
