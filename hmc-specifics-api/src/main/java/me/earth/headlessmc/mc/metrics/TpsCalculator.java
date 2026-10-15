package me.earth.headlessmc.mc.metrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Estimates the TPS of a server from the game time the server sends to the client.
 * Servers send their game time about once per second (every 20 ticks),
 * the game time advances by one every tick, so the TPS are the ticks that passed between two samples,
 * divided by the time between receiving them.
 * If no new samples arrive the server is probably not ticking, so the estimate decreases.
 */
public class TpsCalculator {
    private static final double NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    /**
     * If no sample has been received for this long the time since the last sample is taken into account.
     */
    private static final long STALE_NANOS = TimeUnit.SECONDS.toNanos(3);

    private final Deque<Sample> samples = new ArrayDeque<>();
    private final int maxSamples;

    /**
     * @param maxSamples the amount of samples to keep, about the amount of seconds to average over.
     */
    public TpsCalculator(int maxSamples) {
        if (maxSamples < 2) {
            throw new IllegalArgumentException("Need at least two samples, but got " + maxSamples);
        }

        this.maxSamples = maxSamples;
    }

    public synchronized void add(long nanoTime, long gameTime) {
        Sample last = samples.peekLast();
        if (last != null && gameTime < last.gameTime) {
            samples.clear(); // probably joined another server
        }

        samples.addLast(new Sample(nanoTime, gameTime));
        while (samples.size() > maxSamples) {
            samples.removeFirst();
        }
    }

    /**
     * @param nanoTime the current {@link System#nanoTime()}.
     * @param maxTps the maximum TPS to return, usually the tick rate of the server.
     * @return the estimated TPS or {@code null} if not enough samples have been collected yet.
     */
    public synchronized Double getTps(long nanoTime, double maxTps) {
        if (samples.size() < 2) {
            return null;
        }

        Sample first = samples.peekFirst();
        Sample last = samples.peekLast();
        long end = nanoTime - last.nanoTime > STALE_NANOS ? nanoTime : last.nanoTime;
        long elapsed = end - first.nanoTime;
        if (elapsed <= 0) {
            return null;
        }

        double tps = (last.gameTime - first.gameTime) * NANOS_PER_SECOND / elapsed;
        return Math.max(0.0, Math.min(maxTps, tps));
    }

    public synchronized void reset() {
        samples.clear();
    }

    private static final class Sample {
        private final long nanoTime;
        private final long gameTime;

        private Sample(long nanoTime, long gameTime) {
            this.nanoTime = nanoTime;
            this.gameTime = gameTime;
        }
    }

}
