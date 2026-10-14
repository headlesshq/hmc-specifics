package me.earth.headlessmc.mc.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CommandRegistryTest {
    private final ClassLoader classLoader = CommandRegistryTest.class.getClassLoader();

    @Test
    public void testParse() {
        assertNull(CommandRegistry.parse("", "test", classLoader));
        assertNull(CommandRegistry.parse("   # comment", "test", classLoader));
        assertNull(CommandRegistry.parse("com.example.DoesNotExist", "test", classLoader));

        CommandRegistry.Entry entry = CommandRegistry.parse(" " + ConnectCommand.class.getName() + " # comment", "test", classLoader);
        assertNotNull(entry);
        assertEquals(ConnectCommand.class, entry.getType());
        assertEquals(Collections.emptyList(), entry.getParents());

        entry = CommandRegistry.parse("account  token\t" + TokenAccountCommand.class.getName(), "test", classLoader);
        assertNotNull(entry);
        assertEquals(TokenAccountCommand.class, entry.getType());
        assertEquals(Arrays.asList("account", "token"), entry.getParents());
    }

    @Test
    public void testLoad() {
        CommandRegistry registry = CommandRegistry.load(classLoader);
        assertTrue(registry.getCommands().stream().anyMatch(e -> e.getType() == ConnectCommand.class));
        assertTrue(registry.getFactories().containsKey(io.github.headlesshq.headlessmc.commands.auth.AccountCommand.class));
    }

    @Test
    public void testIsUpToDate(@TempDir Path dir) throws Exception {
        CommandRegistry registry = CommandRegistry.load(classLoader);
        assertTrue(registry.isUpToDate(classLoader));

        // e.g. Forge 1.12.2 adds mods to the classpath after HMC-Specifics has been initialized
        Path file = dir.resolve(CommandRegistry.COMMANDS_FILE);
        Files.createDirectories(file.getParent());
        Files.write(file, (ConnectCommand.class.getName() + "\n").getBytes(StandardCharsets.UTF_8));
        try (URLClassLoader withMod = new URLClassLoader(new URL[]{dir.toUri().toURL()}, classLoader)) {
            assertFalse(registry.isUpToDate(withMod));
            CommandRegistry reloaded = CommandRegistry.load(withMod);
            assertTrue(reloaded.isUpToDate(withMod));
            assertEquals(registry.getFiles().size() + 1, reloaded.getFiles().size());
        }
    }

}
