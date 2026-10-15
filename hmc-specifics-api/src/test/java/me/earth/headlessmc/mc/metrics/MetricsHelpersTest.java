package me.earth.headlessmc.mc.metrics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsHelpersTest {
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    @Test
    public void testTpsCalculator() {
        TpsCalculator calculator = new TpsCalculator(5);
        assertNull(calculator.getTps(0, 20.0));
        calculator.add(0, 1000);
        assertNull(calculator.getTps(0, 20.0));
        calculator.add(SECOND, 1020);
        assertEquals(20.0, calculator.getTps(SECOND, 20.0), 0.001);

        // server lags, 10 ticks in a second
        calculator.add(2 * SECOND, 1030);
        assertEquals(15.0, calculator.getTps(2 * SECOND, 20.0), 0.001);

        // clamped to the tick rate
        calculator.reset();
        calculator.add(0, 0);
        calculator.add(SECOND, 25);
        assertEquals(20.0, calculator.getTps(SECOND, 20.0), 0.001);
        assertEquals(25.0, calculator.getTps(SECOND, 50.0), 0.001);
    }

    @Test
    public void testTpsCalculatorStale() {
        TpsCalculator calculator = new TpsCalculator(5);
        calculator.add(0, 0);
        calculator.add(SECOND, 20);
        assertEquals(20.0, calculator.getTps(2 * SECOND, 20.0), 0.001);
        // no samples for 9 seconds, server is probably frozen
        assertEquals(2.0, calculator.getTps(10 * SECOND, 20.0), 0.001);
    }

    @Test
    public void testTpsCalculatorKeepsMaxSamplesAndResetsOnNewWorld() {
        TpsCalculator calculator = new TpsCalculator(3);
        calculator.add(0, 0);
        calculator.add(SECOND, 0); // a second without ticks
        calculator.add(2 * SECOND, 20);
        assertEquals(10.0, calculator.getTps(2 * SECOND, 20.0), 0.001);
        calculator.add(3 * SECOND, 40); // the first sample is dropped
        assertEquals(20.0, calculator.getTps(3 * SECOND, 20.0), 0.001);

        calculator.add(4 * SECOND, 5); // game time went back, new world
        assertNull(calculator.getTps(4 * SECOND, 20.0));
    }

    @Test
    public void testRollingAverage() {
        RollingAverage average = new RollingAverage(3);
        assertNull(average.getAverage());
        assertNull(average.getMax());
        average.add(1.0);
        average.add(2.0);
        assertEquals(1.5, average.getAverage(), 0.001);
        average.add(6.0);
        average.add(4.0); // replaces 1.0
        assertEquals(4.0, average.getAverage(), 0.001);
        assertEquals(6.0, average.getMax(), 0.001);
        average.reset();
        assertNull(average.getAverage());
    }

    @Test
    public void testRateCounter() {
        RateCounter counter = new RateCounter(2);
        assertEquals(0.0, counter.getRate(0));
        for (int i = 0; i < 10; i++) {
            counter.record(SECOND / 10 * i);
        }

        // the current second is not complete yet
        assertEquals(0.0, counter.getRate(SECOND / 2));
        // 10 events in the last 2 completed seconds
        assertEquals(5.0, counter.getRate(SECOND), 0.001);
        for (int i = 0; i < 20; i++) {
            counter.record(SECOND + SECOND / 20 * i);
        }

        assertEquals(15.0, counter.getRate(2 * SECOND), 0.001);
        assertEquals(10.0, counter.getRate(3 * SECOND), 0.001);
        assertEquals(0.0, counter.getRate(10 * SECOND), 0.001);
    }

    @Test
    public void testTracker() {
        AtomicLong time = new AtomicLong();
        MetricsTracker tracker = new MetricsTracker(time::get);
        assertNull(tracker.getAverageTickTime());
        assertNull(tracker.getMillisSinceLastPacket());
        assertNull(tracker.getSecondsSinceJoin());

        tracker.onTickStart();
        time.addAndGet(TimeUnit.MILLISECONDS.toNanos(10));
        tracker.onTickEnd();
        assertEquals(10.0, tracker.getAverageTickTime(), 0.001);
        assertEquals(10.0, tracker.getMaxTickTime(), 0.001);
        tracker.onTickEnd(); // without a start
        assertEquals(10.0, tracker.getAverageTickTime(), 0.001);

        tracker.onJoin();
        tracker.onPacketReceived();
        tracker.onTimeUpdate(0);
        time.addAndGet(SECOND);
        tracker.onTimeUpdate(20);
        assertEquals(20.0, tracker.getTps(null), 0.001);
        assertEquals(10.0, tracker.getTps(10.0f), 0.001);
        assertEquals(1000L, tracker.getMillisSinceLastPacket());
        assertEquals(1L, tracker.getSecondsSinceJoin());

        tracker.onJoin();
        assertNull(tracker.getTps(null));
    }

}
