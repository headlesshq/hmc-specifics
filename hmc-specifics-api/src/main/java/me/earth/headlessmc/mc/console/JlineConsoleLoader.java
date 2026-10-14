package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Loads the JLine bundle packaged into HMC-Specifics (a JLine 3 or JLine 4 jar and our console implementation)
 * on an {@link IsolatedClassLoader} and creates a {@link Console} from it.
 */
public class JlineConsoleLoader {
    public static final String BUNDLE_DIR = "META-INF/hmc-specifics/jline/";
    public static final String CONSOLE_CLASS = "me.earth.headlessmc.mc.jline.isolated.IsolatedJlineConsole";
    private static final Logger LOGGER = Logger.getLogger(JlineConsoleLoader.class.getName());

    private final ClassLoader resourceLoader;

    public JlineConsoleLoader() {
        this(JlineConsoleLoader.class.getClassLoader());
    }

    public JlineConsoleLoader(ClassLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public boolean isBundlePresent() {
        return resourceLoader.getResource(BUNDLE_DIR + "index.txt") != null;
    }

    public Console load(boolean dumb) throws IOException, ReflectiveOperationException {
        List<String> jars = readIndex();
        Path dir = Files.createTempDirectory("hmc-specifics-jline");
        dir.toFile().deleteOnExit();
        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            Path jar = extract(jars.get(i), dir);
            urls[i] = jar.toUri().toURL();
        }

        LOGGER.info("Loading JLine from " + jars);
        IsolatedClassLoader classLoader = new IsolatedClassLoader(urls, Console.class.getClassLoader());
        Class<?> consoleClass = Class.forName(CONSOLE_CLASS, true, classLoader);
        return (Console) consoleClass.getConstructor(boolean.class).newInstance(dumb);
    }

    private List<String> readIndex() throws IOException {
        try (InputStream is = openResource("index.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    result.add(line);
                }
            }

            return result;
        }
    }

    private Path extract(String name, Path dir) throws IOException {
        Path target = dir.resolve(name);
        try (InputStream is = openResource(name)) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        target.toFile().deleteOnExit();
        return target;
    }

    private InputStream openResource(String name) throws IOException {
        InputStream is = resourceLoader.getResourceAsStream(BUNDLE_DIR + name);
        if (is == null) {
            throw new IOException("Failed to find " + BUNDLE_DIR + name);
        }

        return is;
    }

}
