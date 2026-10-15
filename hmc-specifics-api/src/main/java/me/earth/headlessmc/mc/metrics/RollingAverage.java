package me.earth.headlessmc.mc.metrics;

/**
 * Average and maximum of the last {@code size} values.
 */
public class RollingAverage {
    private final double[] values;
    private int index;
    private int count;

    public RollingAverage(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive, but was " + size);
        }

        this.values = new double[size];
    }

    public synchronized void add(double value) {
        values[index] = value;
        index = (index + 1) % values.length;
        count = Math.min(count + 1, values.length);
    }

    /**
     * @return the average of the values or {@code null} if no values have been added.
     */
    public synchronized Double getAverage() {
        if (count == 0) {
            return null;
        }

        double sum = 0.0;
        for (int i = 0; i < count; i++) {
            sum += values[i];
        }

        return sum / count;
    }

    /**
     * @return the maximum of the values or {@code null} if no values have been added.
     */
    public synchronized Double getMax() {
        if (count == 0) {
            return null;
        }

        double max = values[0];
        for (int i = 1; i < count; i++) {
            max = Math.max(max, values[i]);
        }

        return max;
    }

    public synchronized void reset() {
        index = 0;
        count = 0;
    }

}
