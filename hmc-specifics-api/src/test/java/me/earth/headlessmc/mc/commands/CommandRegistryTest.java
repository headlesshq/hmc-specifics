package me.earth.headlessmc.mc.commands;

import org.junit.jupiter.api.Test;

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

}
