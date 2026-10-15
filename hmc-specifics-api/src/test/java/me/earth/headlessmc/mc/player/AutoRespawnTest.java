package me.earth.headlessmc.mc.player;

import me.earth.headlessmc.mc.MockMc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutoRespawnTest {
    @AfterEach
    public void reset() {
        MockMc.INSTANCE.player = null;
    }

    @Test
    public void testDisabledByDefault() {
        assertFalse(AutoRespawn.getInstance().isEnabled());
    }

    @Test
    public void testRespawnsWhenDead() {
        AutoRespawn autoRespawn = new AutoRespawn(true);
        MockPlayer player = new MockPlayer();
        MockMc.INSTANCE.player = player;

        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(0, player.respawnRequests);

        player.dead = true;
        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(1, player.respawnRequests);

        for (int i = 0; i < AutoRespawn.RETRY_TICKS; i++) {
            autoRespawn.onTick(MockMc.INSTANCE);
        }

        assertEquals(1, player.respawnRequests);
        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(2, player.respawnRequests);

        player.dead = false;
        autoRespawn.onTick(MockMc.INSTANCE);
        player.dead = true;
        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(3, player.respawnRequests);
    }

    @Test
    public void testDoesNothingWhenDisabled() {
        AutoRespawn autoRespawn = new AutoRespawn(false);
        MockPlayer player = new MockPlayer();
        player.dead = true;
        MockMc.INSTANCE.player = player;

        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(0, player.respawnRequests);

        autoRespawn.setEnabled(true);
        autoRespawn.onTick(MockMc.INSTANCE);
        assertEquals(1, player.respawnRequests);
    }

    @Test
    public void testNotIngame() {
        new AutoRespawn(true).onTick(MockMc.INSTANCE);
    }

}
