package me.earth.headlessmc.mc.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Collects metrics from events, the version specific mixins call the {@code on...} methods.
 * The methods can be called from any thread.
 */
public class MetricsTracker {
    private static final MetricsTracker INSTANCE = new MetricsTracker(System::nanoTime);
    private static final double NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);
    private static final double DEFAULT_TICK_RATE = 20.0;

    private final RollingAverage tickTimes = new RollingAverage(100);
    private final TpsCalculator tpsCalculator = new TpsCalculator(20);
    private final RateCounter received = new RateCounter(5);
    private final RateCounter sent = new RateCounter(5);
    private final LongSupplier nanoTime;

    private volatile long tickStart = -1L;
    private volatile long lastPacket = -1L;
    private volatile long joined = -1L;

    public MetricsTracker(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    public static MetricsTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Called before Minecraft ticks.
     */
    public void onTickStart() {
        tickStart = nanoTime.getAsLong();
    }

    /**
     * Called after Minecraft ticked.
     */
    public void onTickEnd() {
        long start = tickStart;
        if (start != -1L) {
            tickTimes.add((nanoTime.getAsLong() - start) / NANOS_PER_MILLI);
            tickStart = -1L;
        }
    }

    /**
     * Called when the server sends its game time, which it does once every 20 ticks.
     *
     * @param gameTime the game time sent by the server.
     */
    public void onTimeUpdate(long gameTime) {
        tpsCalculator.add(nanoTime.getAsLong(), gameTime);
    }

    /**
     * Called when the client joins a world.
     */
    public void onJoin() {
        joined = nanoTime.getAsLong();
        tpsCalculator.reset();
    }

    /**
     * Called when the client receives a packet.
     */
    public void onPacketReceived() {
        long now = nanoTime.getAsLong();
        lastPacket = now;
        received.record(now);
    }

    /**
     * Called when the client sends a packet.
     */
    public void onPacketSent() {
        sent.record(nanoTime.getAsLong());
    }

    /**
     * @return the average time Minecraft took for its last 100 ticks in milliseconds, or {@code null}.
     */
    public Double getAverageTickTime() {
        return tickTimes.getAverage();
    }

    /**
     * @return the maximum time Minecraft took for one of its last 100 ticks in milliseconds, or {@code null}.
     */
    public Double getMaxTickTime() {
        return tickTimes.getMax();
    }

    /**
     * @param tickRate the tick rate of the server, or {@code null} for the default of 20.
     * @return the estimated server TPS or {@code null} if they could not be estimated yet.
     */
    public Double getTps(Float tickRate) {
        return tpsCalculator.getTps(nanoTime.getAsLong(), tickRate == null ? DEFAULT_TICK_RATE : tickRate);
    }

    public double getPacketsReceivedPerSecond() {
        return received.getRate(nanoTime.getAsLong());
    }

    public double getPacketsSentPerSecond() {
        return sent.getRate(nanoTime.getAsLong());
    }

    /**
     * @return the milliseconds since the last packet has been received, or {@code null}.
     */
    public Long getMillisSinceLastPacket() {
        long last = lastPacket;
        return last == -1L ? null : TimeUnit.NANOSECONDS.toMillis(nanoTime.getAsLong() - last);
    }

    /**
     * @return the seconds since the client joined the current world, or {@code null}.
     */
    public Long getSecondsSinceJoin() {
        long join = joined;
        return join == -1L ? null : TimeUnit.NANOSECONDS.toSeconds(nanoTime.getAsLong() - join);
    }

}
