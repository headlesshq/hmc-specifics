package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.ArgSplitter;
import io.github.headlesshq.headlessmc.console.Console;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JlineLoaderTest {
    @ParameterizedTest
    @ValueSource(strings = {"jline3", "jline4"})
    public void testLoadBundle(String jline) throws Exception {
        String bundle = System.getProperty("hmc.test." + jline + ".bundle");
        assumeTrue(bundle != null, "Bundle for " + jline + " not built, JLine 4 requires Java 11");
        Path bundlePath = Paths.get(bundle);
        try (URLClassLoader bundleLoader = new URLClassLoader(new URL[]{bundlePath.toUri().toURL()}, null)) {
            JlineLoader loader = new JlineLoader(bundleLoader);
            assertTrue(loader.isBundlePresent());
            Console console = loader.loadConsole(true);
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
                    .loadClass(JlineLoaderTest.class.getName()));
                console.write("Test");

                ArgSplitter argSplitter = loader.loadArgSplitter();
                // console and arg splitter share the same classloader
                assertSame(console.getClass().getClassLoader(), argSplitter.getClass().getClassLoader());
                assertArrayEquals(new String[]{"a", "b"}, argSplitter.split("a b "));
                assertArrayEquals(new String[]{"text", "hello world", "x"}, argSplitter.split("text \"hello world\" x"));
                assertArrayEquals(new String[]{"a b", "c"}, argSplitter.split("a\\ b c"));
            } finally {
                ((AutoCloseable) console).close();
            }
        }
    }

}
