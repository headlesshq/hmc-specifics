package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JlineConsoleLoaderTest {
    @ParameterizedTest
    @ValueSource(strings = {"jline3", "jline4"})
    public void testLoadBundle(String jline) throws Exception {
        String bundle = System.getProperty("hmc.test." + jline + ".bundle");
        assumeTrue(bundle != null, "Bundle for " + jline + " not built, JLine 4 requires Java 11");
        Path bundlePath = Paths.get(bundle);
        try (URLClassLoader bundleLoader = new URLClassLoader(new URL[]{bundlePath.toUri().toURL()}, null)) {
            JlineConsoleLoader loader = new JlineConsoleLoader(bundleLoader);
            assertTrue(loader.isBundlePresent());
            Console console = loader.load(true);
            try {
                assertInstanceOf(IsolatedClassLoader.class, console.getClass().getClassLoader());
                assertTrue(console.extensions().isPresent());
                // the API is shared with the parent classloader
                assertSame(Console.class, console.getClass().getInterfaces()[0]);
                // JLine is not, it gets loaded from the bundle
                Class<?> terminal = console.getClass().getClassLoader().loadClass("org.jline.terminal.Terminal");
                assertSame(console.getClass().getClassLoader(), terminal.getClassLoader());
                // other classes from the parent are not visible
                assertThrows(ClassNotFoundException.class, () -> console.getClass().getClassLoader()
                    .loadClass(JlineConsoleLoaderTest.class.getName()));
                console.write("Test");
            } finally {
                ((AutoCloseable) console).close();
            }
        }
    }

}
