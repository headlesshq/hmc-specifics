package me.earth.headlessmc.mc.player;

import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.SpecificProperties;

import java.util.logging.Logger;

/**
 * Respawns the player automatically when they die.
 * Enabled on startup by launching the game with {@code -Dhmc.respawn.auto=true},
 * can be toggled with the {@code respawn auto} command.
 * The version specific mixins call {@link #onTick(Minecraft)} at the end of every client tick.
 */
public final class AutoRespawn {
    private static final Logger LOGGER = Logger.getLogger(AutoRespawn.class.getName());
    private static final AutoRespawn INSTANCE = new AutoRespawn(SpecificProperties.getBoolean(SpecificProperties.AUTO_RESPAWN, false));
    /**
     * If the server does not respawn us, we ask again after this many ticks.
     */
    static final int RETRY_TICKS = 20;

    private volatile boolean enabled;
    private int ticksUntilRespawn;
    private boolean dead;

    AutoRespawn(boolean enabled) {
        this.enabled = enabled;
    }

    public static AutoRespawn getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Called on Minecraft's main thread at the end of every tick.
     *
     * @param mc the Minecraft instance to check the player of.
     */
    public void onTick(Minecraft mc) {
        Player player = mc.getPlayer();
        if (!enabled || player == null || !player.isDead()) {
            dead = false;
            return;
        }

        if (!dead) {
            dead = true;
            ticksUntilRespawn = 0;
            LOGGER.info("Player died, respawning automatically.");
        }

        if (ticksUntilRespawn-- <= 0) {
            ticksUntilRespawn = RETRY_TICKS;
            player.requestRespawn();
        }
    }

}
