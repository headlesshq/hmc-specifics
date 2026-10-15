package me.earth.headlessmc.mc.metrics;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Counts events per second, averaged over the last completed seconds.
 * Thread-safe, events can e.g. be recorded on Netty threads.
 */
public class RateCounter {
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    private final long[] buckets;
    private long currentSecond = Long.MIN_VALUE;

    /**
     * @param seconds the amount of completed seconds to average over.
     */
    public RateCounter(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Seconds must be positive, but was " + seconds);
        }

        // one more bucket for the current, incomplete second
        this.buckets = new long[seconds + 1];
    }

    public synchronized void record(long nanoTime) {
        advance(nanoTime);
        buckets[index(currentSecond)]++;
    }

    /**
     * @param nanoTime the current {@link System#nanoTime()}.
     * @return the average amount of events per second over the last completed seconds.
     */
    public synchronized double getRate(long nanoTime) {
        if (currentSecond == Long.MIN_VALUE) {
            return 0.0;
        }

        advance(nanoTime);
        long sum = 0;
        for (int i = 1; i < buckets.length; i++) {
            sum += buckets[index(currentSecond - i)];
        }

        return sum / (double) (buckets.length - 1);
    }

    public synchronized void reset() {
        currentSecond = Long.MIN_VALUE;
        Arrays.fill(buckets, 0L);
    }

    private void advance(long nanoTime) {
        long second = Math.floorDiv(nanoTime, SECOND);
        if (currentSecond == Long.MIN_VALUE || second - currentSecond >= buckets.length) {
            Arrays.fill(buckets, 0L);
        } else {
            for (long s = currentSecond + 1; s <= second; s++) {
                buckets[index(s)] = 0L;
            }
        }

        if (currentSecond == Long.MIN_VALUE || second > currentSecond) {
            currentSecond = second;
        }
    }

    private int index(long second) {
        return (int) Math.floorMod(second, (long) buckets.length);
    }

}
