package me.earth.headlessmc.mc;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for HMC-Specifics, read from System properties, e.g. {@code -Dhmc.jline.enabled=false}.
 */
public final class SpecificProperties {
    public static final String JLINE_ENABLED = "hmc.jline.enabled";
    public static final String JLINE_DUMB = "hmc.jline.dumb";
    public static final String DEBUG_JLINE = "hmc.debug.jline";

    public static final String ACCOUNT_REFRESH_ENABLED = "hmc.account.refresh.enabled";
    /**
     * Interval in seconds.
     */
    public static final String REFRESH_INTERVAL = "hmc.account.refresh.interval";

    public static final String AUTO_ACCEPT_SERVER_RESOURCE_PACKS = "hmc.resourcepacks.autoaccept";

    /**
     * Whether {@link me.earth.headlessmc.mc.player.AutoRespawn} is enabled when the game starts.
     */
    public static final String AUTO_RESPAWN = "hmc.respawn.auto";

    /**
     * The directory HeadlessMC places its files in, same as HeadlessMC's {@code hmc.files.location}.
     * Can also be set via the {@code HMC_FILES_LOCATION} environment variable.
     */
    public static final String FILES_LOCATION = "hmc.files.location";

    private SpecificProperties() {
        throw new AssertionError();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = System.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public static long getLong(String key, long defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getRefreshIntervalSeconds() {
        return getLong(REFRESH_INTERVAL, TimeUnit.HOURS.toSeconds(23));
    }

}
