package me.earth.headlessmc.mc.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collects the picocli commands of HMC-Specifics and of other mods.
 * Commands are listed in {@value #COMMANDS_FILE} files, one class name per line.
 * Subcommands of an existing command are prefixed with the names of their parent commands, e.g.:
 * <pre>
 * # comment
 * com.example.MyCommand
 * account com.example.MyAccountSubCommand
 * </pre>
 * Classes that can not be instantiated by the {@link PicocliFactory} need a {@link CommandFactory}.
 * The order in which the files of different mods are read is not defined,
 * so commands are sorted by their number of parents, parent commands are always registered before their subcommands.
 */
public class CommandRegistry {
    public static final String COMMANDS_FILE = "META-INF/hmc-specifics-commands";
    private static final Logger LOGGER = Logger.getLogger(CommandRegistry.class.getName());

    private final List<Entry> commands;
    private final Map<Class<?>, CommandFactory<?>> factories;

    public CommandRegistry(List<Entry> commands, Map<Class<?>, CommandFactory<?>> factories) {
        List<Entry> sorted = new ArrayList<>(commands);
        sorted.sort(Comparator.comparingInt(entry -> entry.getParents().size())); // stable, keeps the order of each file
        this.commands = Collections.unmodifiableList(sorted);
        this.factories = Collections.unmodifiableMap(new HashMap<>(factories));
    }

    public static CommandRegistry load(ClassLoader classLoader) {
        return new CommandRegistry(loadCommands(classLoader), loadFactories(classLoader));
    }

    public List<Entry> getCommands() {
        return commands;
    }

    public Map<Class<?>, CommandFactory<?>> getFactories() {
        return factories;
    }

    private static List<Entry> loadCommands(ClassLoader classLoader) {
        Set<String> urls = new LinkedHashSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(COMMANDS_FILE);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement().toString());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to find " + COMMANDS_FILE + " files", e);
        }

        List<Entry> result = new ArrayList<>();
        for (String url : urls) {
            try (InputStream is = new URL(url).openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Entry entry = parse(line, url, classLoader);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read " + url, e);
            }
        }

        return result;
    }

    static Entry parse(String line, String source, ClassLoader classLoader) {
        int comment = line.indexOf('#');
        if (comment >= 0) {
            line = line.substring(0, comment);
        }

        line = line.trim();
        if (line.isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\s+");
        String className = parts[parts.length - 1];
        try {
            Class<?> type = Class.forName(className, false, classLoader);
            return new Entry(Arrays.asList(parts).subList(0, parts.length - 1), type);
        } catch (ClassNotFoundException | LinkageError e) {
            LOGGER.log(Level.WARNING, "Failed to load command " + className + " from " + source, e);
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class<?>, CommandFactory<?>> loadFactories(ClassLoader classLoader) {
        Map<Class<?>, CommandFactory<?>> result = new HashMap<>();
        Iterator<CommandFactory> iterator = ServiceLoader.load(CommandFactory.class, classLoader).iterator();
        while (true) {
            CommandFactory<?> factory;
            try {
                if (!iterator.hasNext()) {
                    break;
                }

                factory = iterator.next();
            } catch (ServiceConfigurationError e) {
                LOGGER.log(Level.WARNING, "Failed to load CommandFactory", e);
                continue;
            }

            CommandFactory<?> existing = result.putIfAbsent(factory.getType(), factory);
            if (existing != null) {
                LOGGER.warning("Ignoring CommandFactory " + factory.getClass().getName() + " for " + factory.getType().getName()
                                   + ", already registered by " + existing.getClass().getName());
            }
        }

        return result;
    }

    /**
     * A command class and the names of its parent commands, empty for top level commands.
     */
    public static final class Entry {
        private final List<String> parents;
        private final Class<?> type;

        public Entry(List<String> parents, Class<?> type) {
            this.parents = Collections.unmodifiableList(new ArrayList<>(parents));
            this.type = type;
        }

        public List<String> getParents() {
            return parents;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public String toString() {
            return parents.isEmpty() ? type.getName() : String.join(" ", parents) + " " + type.getName();
        }
    }

}
