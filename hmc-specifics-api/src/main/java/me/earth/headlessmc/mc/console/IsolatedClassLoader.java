package me.earth.headlessmc.mc.console;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A classloader that does not see any classes of the classloader that loaded HMC-Specifics,
 * except for the {@code io.github.headlesshq.headlessmc} API, e.g. {@link io.github.headlesshq.headlessmc.console.Console}.
 * Everything else is loaded from the given urls, or the JDK.
 * This way we can use our own JLine version, even if the game or the modloader ship their own.
 */
public class IsolatedClassLoader extends URLClassLoader {
    public static final String SHARED_PACKAGE = "io.github.headlesshq.headlessmc.";

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final ClassLoader apiClassLoader;

    public IsolatedClassLoader(URL[] urls, ClassLoader apiClassLoader) {
        super(urls, findParentClassLoader());
        this.apiClassLoader = apiClassLoader;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith(SHARED_PACKAGE)) {
            return apiClassLoader.loadClass(name);
        }

        return super.loadClass(name, resolve);
    }

    /**
     * @return the platform classloader on Java 9+, {@code null} (the bootstrap classloader) on Java 8.
     */
    private static ClassLoader findParentClassLoader() {
        try {
            return (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

}
