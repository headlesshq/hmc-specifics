package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.MockMc;
import me.earth.headlessmc.mc.auth.AccountRefreshingService;
import me.earth.headlessmc.mc.auth.HmcAuth;
import me.earth.headlessmc.mc.auth.McLastUsedAccountService;
import me.earth.headlessmc.mc.console.RecordingConsole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftContextTest {
    private RecordingConsole console;
    private MinecraftContext ctx;

    @BeforeEach
    public void setup(@TempDir Path authDir) {
        console = new RecordingConsole();
        HmcAuth auth = new HmcAuth(authDir);
        AccountRefreshingService refreshingService = new AccountRefreshingService(auth.getAuthService(), MockMc.INSTANCE);
        ctx = new MinecraftContext(
            MockMc.INSTANCE,
            console,
            line -> line.trim().split("\\s+"),
            auth.getAuthService(),
            new McLastUsedAccountService(auth.getLastUsedAccountService(), refreshingService, MockMc.INSTANCE),
            refreshingService
        );
    }

    @Test
    public void testConnectAndDisconnect() {
        MockMc.INSTANCE.disconnect();
        assertNull(MockMc.INSTANCE.ip);
        assertEquals(-1, MockMc.INSTANCE.port);

        ctx.execute("connect 2b2t.org");
        assertEquals("2b2t.org", MockMc.INSTANCE.ip);
        assertEquals(25565, MockMc.INSTANCE.port);

        ctx.execute("disconnect");
        assertNull(MockMc.INSTANCE.ip);
        assertEquals(-1, MockMc.INSTANCE.port);

        ctx.execute("connect 2b2t.org 500");
        assertEquals("2b2t.org", MockMc.INSTANCE.ip);
        assertEquals(500, MockMc.INSTANCE.port);

        ctx.execute("connect 2b2t.org notAPort");
        assertEquals(500, MockMc.INSTANCE.port);
        assertTrue(console.joined().contains("notAPort"), console.joined());
    }

    @Test
    public void testUnknownCommand() {
        ctx.execute("conect 2b2t.org");
        assertTrue(console.joined().contains("Couldn't find command for 'conect 2b2t.org'"), console.joined());
        assertTrue(console.joined().contains("connect"), console.joined());
    }

    @Test
    public void testHelp() {
        ctx.execute("help");
        String help = console.joined();
        assertTrue(help.contains("gui"), help);
        assertTrue(help.contains("account"), help);
        assertTrue(help.contains("msg <message>"), help);

        console.output.clear();
        ctx.execute("connect --help");
        assertTrue(console.joined().contains("Connects you to a server."), console.joined());
    }

    @Test
    public void testMessageNotIngame() {
        ctx.execute("msg hello it's me");
        assertEquals("You need to be ingame to send a message!", console.joined());

        console.output.clear();
        ctx.execute("msg");
        assertEquals("Please specify a message", console.joined());

        console.output.clear();
        ctx.execute("/gamemode creative");
        assertEquals("You need to be ingame to send a command!", console.joined());
    }

    @Test
    public void testTokenAccount() {
        UUID uuid = UUID.randomUUID();
        ctx.execute("account token Test " + uuid + " token123");
        assertNotNull(MockMc.INSTANCE.account);
        assertEquals("Test", MockMc.INSTANCE.account.getName());
        assertEquals(uuid, MockMc.INSTANCE.account.getUuid());
        assertEquals("token123", MockMc.INSTANCE.account.getAccessToken());

        console.output.clear();
        ctx.execute("login current");
        assertEquals("Current account: Test", console.joined());
    }

    @Test
    public void testGuiWithoutScreen() {
        ctx.execute("gui");
        assertEquals("Minecraft is currently not displaying a Gui.", console.joined());
    }

    @Test
    public void testExternalCommands() {
        ctx.execute("greet Steve");
        assertEquals("Hello Steve", console.joined());

        console.output.clear();
        ctx.execute("account info");
        assertEquals("Account info", console.joined());

        // the broken entries in the test commands file are skipped
        assertTrue(ctx.getCommands().stream().noneMatch(e -> e.getParents().contains("doesnotexist")));
    }

    @Test
    public void testTestMod() {
        // hmc-specifics-test-mod.jar is on the test runtime classpath, its commands are used by hmc-specifics-test.json
        ctx.execute("hmcs-test Steve");
        assertEquals("[hmcs-test] Hello Steve!", console.joined());

        console.output.clear();
        ctx.execute("hmcs-test scheduled");
        assertEquals("[hmcs-test] Scheduled command executed.", console.joined());

        console.output.clear();
        ctx.execute("hmcs-test sleep 0");
        assertEquals("[hmcs-test] Slept 0s.", console.joined());

        console.output.clear();
        ctx.execute("account hmcs-test");
        assertEquals("[hmcs-test] Account subcommand executed.", console.joined());
    }

    @Test
    public void testAccountCompletionsFactory() throws Exception {
        PicocliFactory factory = new PicocliFactory(ctx, CommandRegistry.load(getClass().getClassLoader()).getFactories());
        assertNotNull(factory.create(io.github.headlesshq.headlessmc.commands.auth.AccountCompletions.class));
        assertNotNull(factory.create(io.github.headlesshq.headlessmc.commands.auth.AuthProviderCompletions.class));
    }

    @Test
    public void testParseUuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, HmcAuth.parseUuid(uuid.toString()));
        assertEquals(uuid, HmcAuth.parseUuid(uuid.toString().replace("-", "")));
    }

}
