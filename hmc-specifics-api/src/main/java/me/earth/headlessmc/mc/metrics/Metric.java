package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.util.HasName;

import java.util.function.Supplier;

/**
 * A value that can be collected from the running game, e.g. the estimated server TPS.
 * Names are dot separated, the first part is the group of the metric, e.g. {@code server.tps}.
 * Values should be a {@link Number}, {@link String}, {@link Boolean},
 * a {@link java.util.Map} with String keys or a {@link java.util.List} of such values,
 * so that they can be displayed by the {@link MetricsFormatter}.
 * Metrics that access Minecraft have to be collected on Minecraft's main thread.
 *
 * @param <T> the type of value of this metric.
 */
public interface Metric<T> extends HasName {
    /**
     * @return a short description of this metric.
     */
    String getDescription();

    /**
     * @return the current value of this metric or {@code null} if it is currently not available.
     */
    T get();

    /**
     * @return the part of the name in front of the first dot, e.g. {@code server} for {@code server.tps}.
     */
    default String getGroup() {
        int index = getName().indexOf('.');
        return index < 0 ? getName() : getName().substring(0, index);
    }

    static <T> Metric<T> of(String name, String description, Supplier<T> supplier) {
        return new Metric<T>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public T get() {
                return supplier.get();
            }

            @Override
            public String toString() {
                return "Metric{" + name + "}";
            }
        };
    }

}
