package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A registry for {@link Metric}s.
 * HMC-Specifics registers its {@link DefaultMetrics} on {@link #getInstance()} when it initializes.
 * Other mods can register their own metrics, either directly or with a {@link MetricsProvider}.
 * Most metrics access Minecraft, so they have to be collected on Minecraft's main thread, e.g.:
 * <pre>{@code
 * MetricsService metrics = MetricsService.getInstance();
 * Future<Map<String, Object>> values = mc.schedule(() -> metrics.collect(metrics.select("server.tps", "players")));
 * }</pre>
 */
public class MetricsService {
    private static final Logger LOGGER = Logger.getLogger(MetricsService.class.getName());
    private static final MetricsService INSTANCE = new MetricsService();

    private final Map<String, Metric<?>> metrics = new LinkedHashMap<>();
    private final Set<String> loadedProviders = new LinkedHashSet<>();

    public static MetricsService getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a metric, replacing any metric with the same name.
     *
     * @param metric the metric to register.
     * @return the metric that has been replaced or {@code null}.
     */
    public synchronized Metric<?> register(Metric<?> metric) {
        Metric<?> previous = metrics.put(metric.getName(), metric);
        if (previous != null) {
            LOGGER.warning("Metric " + metric.getName() + " has been replaced by " + metric);
        }

        return previous;
    }

    public synchronized Metric<?> unregister(String name) {
        return metrics.remove(name);
    }

    public synchronized Metric<?> get(String name) {
        return metrics.get(name);
    }

    /**
     * @return all registered metrics, in the order they have been registered in.
     */
    public synchronized List<Metric<?>> getMetrics() {
        return Collections.unmodifiableList(new ArrayList<>(metrics.values()));
    }

    /**
     * Selects metrics, a query matches, ignoring case:
     * <ul>
     *     <li>the full name of a metric, e.g. {@code server.tps},</li>
     *     <li>a group, e.g. {@code server} for all metrics starting with {@code server.},</li>
     *     <li>the last part of the name, e.g. {@code tps} for {@code server.tps}.</li>
     * </ul>
     *
     * @param queries the queries, if none are given all metrics are returned.
     * @return the selected metrics, in the order of the queries.
     * @throws IllegalArgumentException if a query does not match any metric.
     */
    public List<Metric<?>> select(String... queries) {
        List<Metric<?>> all = getMetrics();
        if (queries == null || queries.length == 0) {
            return all;
        }

        Set<Metric<?>> result = new LinkedHashSet<>();
        for (String query : queries) {
            List<Metric<?>> matches = select(all, query);
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("Unknown metric '" + query + "'. Use 'metrics --list' to list all metrics.");
            }

            result.addAll(matches);
        }

        return new ArrayList<>(result);
    }

    /**
     * Gets the values of the given metrics.
     * Exceptions thrown by a metric are logged and its value will be {@code null}.
     *
     * @param metrics the metrics to collect.
     * @return a map from the name of each metric to its value.
     */
    public Map<String, Object> collect(Collection<? extends Metric<?>> metrics) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Metric<?> metric : metrics) {
            Object value;
            try {
                value = metric.get();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to collect metric " + metric.getName(), e);
                value = null;
            }

            result.put(metric.getName(), value);
        }

        return result;
    }

    /**
     * Calls all {@link MetricsProvider}s registered via {@link ServiceLoader} that have not been called yet.
     * Some modloaders, e.g. Forge before 1.13, add mods to the classpath after HMC-Specifics has been initialized,
     * so this is called again before the metrics command runs.
     *
     * @param mc the Minecraft instance to pass to the providers.
     * @param classLoader the ClassLoader to load the providers with.
     */
    public synchronized void loadProviders(Minecraft mc, ClassLoader classLoader) {
        Iterator<MetricsProvider> iterator = ServiceLoader.load(MetricsProvider.class, classLoader).iterator();
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break;
                }

                MetricsProvider provider = iterator.next();
                if (loadedProviders.add(provider.getClass().getName())) {
                    provider.register(this, mc);
                }
            } catch (ServiceConfigurationError | RuntimeException e) {
                LOGGER.log(Level.WARNING, "Failed to load MetricsProvider", e);
            }
        }
    }

    private static List<Metric<?>> select(List<Metric<?>> metrics, String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        List<Metric<?>> result = new ArrayList<>();
        for (Metric<?> metric : metrics) {
            if (metric.getName().toLowerCase(Locale.ROOT).equals(lower)) {
                result.add(metric);
                return result;
            }
        }

        for (Metric<?> metric : metrics) {
            String name = metric.getName().toLowerCase(Locale.ROOT);
            if (name.startsWith(lower + ".") || name.endsWith("." + lower)) {
                result.add(metric);
            }
        }

        return result;
    }

}
