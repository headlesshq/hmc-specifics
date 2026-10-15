package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.Minecraft;

/**
 * Lets other mods register their own {@link Metric}s when HMC-Specifics initializes.
 * Implementations are loaded via {@link java.util.ServiceLoader},
 * list them in {@code META-INF/services/me.earth.headlessmc.mc.metrics.MetricsProvider}.
 * Implementations need a public no-arg constructor.
 */
public interface MetricsProvider {
    /**
     * Registers metrics.
     *
     * @param service the service to register the metrics on.
     * @param mc the Minecraft instance.
     */
    void register(MetricsService service, Minecraft mc);

}
