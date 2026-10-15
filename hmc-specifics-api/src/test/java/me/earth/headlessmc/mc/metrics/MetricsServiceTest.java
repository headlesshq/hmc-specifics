package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.MockMc;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsServiceTest {
    @Test
    public void testSelect() {
        MetricsService service = new MetricsService();
        service.register(Metric.of("server.tps", "", () -> 20.0));
        service.register(Metric.of("server.ping", "", () -> 5));
        service.register(Metric.of("players.ping", "", () -> null));
        service.register(Metric.of("jvm.memory", "", () -> null));

        assertEquals(4, service.select().size());
        assertEquals(names("server.tps"), names(service.select("server.tps")));
        assertEquals(names("server.tps"), names(service.select("TPS")));
        assertEquals(names("server.tps", "server.ping"), names(service.select("server")));
        assertEquals(names("server.ping", "players.ping"), names(service.select("ping")));
        assertEquals(names("jvm.memory", "server.tps"), names(service.select("jvm", "tps", "jvm.memory")));
        assertThrows(IllegalArgumentException.class, () -> service.select("tp"));
        assertThrows(IllegalArgumentException.class, () -> service.select("server."));
    }

    @Test
    public void testRegisterAndCollect() {
        MetricsService service = new MetricsService();
        service.register(Metric.of("a.value", "", () -> 1));
        service.register(Metric.of("a.failing", "", () -> {
            throw new IllegalStateException("Test");
        }));
        assertNotNull(service.register(Metric.of("a.value", "", () -> 2)));

        Map<String, Object> values = service.collect(service.select());
        assertEquals(2, values.get("a.value"));
        assertTrue(values.containsKey("a.failing"));
        assertNull(values.get("a.failing"));

        assertNotNull(service.unregister("a.failing"));
        assertNull(service.get("a.failing"));
    }

    @Test
    public void testDefaultMetricsNotInWorld() {
        MetricsService service = new MetricsService();
        DefaultMetrics.registerAll(service, MockMc.INSTANCE, new MetricsTracker(System::nanoTime));
        Map<String, Object> values = service.collect(service.select());
        assertEquals(false, values.get("server.connected"));
        assertNull(values.get("server.tps"));
        assertNull(values.get("players.list"));
        assertNotNull(values.get("jvm.memory"));
        assertNotNull(values.get("jvm.processors"));
        // the text and json output can be created
        assertTrue(MetricsFormatter.toText(values).contains("server.tps: n/a"));
        assertTrue(MetricsFormatter.toJson(values).contains("\"server.tps\":null"));
    }

    @Test
    public void testDefaultMetricsInWorld() {
        MetricsService service = new MetricsService();
        MockMc.INSTANCE.metricsSource = new TestSource();
        try {
            DefaultMetrics.registerAll(service, MockMc.INSTANCE, new MetricsTracker(System::nanoTime));
        } finally {
            MockMc.INSTANCE.metricsSource = MetricsSource.EMPTY;
        }

        Map<String, Object> values = service.collect(service.select());
        assertEquals("1.21.10", values.get("client.version"));
        assertEquals(true, values.get("server.connected"));
        assertEquals(42, values.get("server.ping"));
        assertEquals(2, values.get("players.count"));
        assertEquals("rain", values.get("world.weather"));
        @SuppressWarnings("unchecked")
        Map<String, Object> ping = (Map<String, Object>) values.get("players.ping");
        assertEquals(42, ping.get("min"));
        assertEquals(71.0, (Double) ping.get("avg"), 0.001);
        assertEquals(100, ping.get("max"));
        @SuppressWarnings("unchecked")
        Map<String, Object> integrated = (Map<String, Object>) values.get("server.integrated");
        assertEquals(20.0, (Double) integrated.get("tps"), 0.001);
        @SuppressWarnings("unchecked")
        Map<String, Object> packets = (Map<String, Object>) values.get("server.packets");
        assertEquals(3.0, packets.get("in"));
        assertEquals(0.0, packets.get("out"));
    }

    @Test
    public void testFormatter() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("a.double", 19.987);
        values.put("a.map", map("x", 1, "y", "two"));
        values.put("a.table", Arrays.asList(map("name", "Steve", "ping", 5), map("name", "Alex", "ping", 100)));
        values.put("a.empty", Collections.emptyList());
        values.put("a.null", null);
        String text = MetricsFormatter.toText(values);
        assertEquals("a.double: 19.99\n"
                         + "a.map: x=1, y=two\n"
                         + "a.table:\n"
                         + "  name    ping\n"
                         + "  Steve   5\n"
                         + "  Alex    100\n"
                         + "a.empty: none\n"
                         + "a.null: n/a", text);
        assertEquals("{\"a.double\":19.987,\"a.map\":{\"x\":1,\"y\":\"two\"},"
                         + "\"a.table\":[{\"name\":\"Steve\",\"ping\":5},{\"name\":\"Alex\",\"ping\":100}],"
                         + "\"a.empty\":[],\"a.null\":null}", MetricsFormatter.toJson(values));
        assertEquals("\"a\\\"b\\\\c\\n\\u0001\"", MetricsFormatter.toJson("a\"b\\c\n\u0001"));
        assertEquals("null", MetricsFormatter.toJson(Double.NaN));
    }

    @Test
    public void testGetLocation() {
        assertEquals("minecraft:overworld", MetricsSource.getLocation("ResourceKey[minecraft:dimension / minecraft:overworld]"));
        assertEquals("other", MetricsSource.getLocation("other"));
        assertNull(MetricsSource.getLocation(null));
    }

    private static Map<String, Object> map(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }

        return map;
    }

    private static List<String> names(String... names) {
        return Arrays.asList(names);
    }

    private static List<String> names(List<Metric<?>> metrics) {
        return metrics.stream().map(Metric::getName).collect(Collectors.toList());
    }

    public static class TestSource implements MetricsSource {
        @Override
        public String getMinecraftVersion() {
            return "1.21.10";
        }

        @Override
        public boolean isInWorld() {
            return true;
        }

        @Override
        public Double getIntegratedServerMspt() {
            return 10.0;
        }

        @Override
        public Float getPacketsReceivedPerSecond() {
            return 3.0f;
        }

        @Override
        public List<PlayerListEntry> getPlayerList() {
            return Arrays.asList(new PlayerListEntry("Steve", UUID.randomUUID(), 42, "survival"),
                                 new PlayerListEntry("Alex", null, 100, null));
        }

        @Override
        public String getPlayerName() {
            return "Steve";
        }

        @Override
        public Boolean isRaining() {
            return true;
        }

        @Override
        public Boolean isThundering() {
            return false;
        }
    }

}
