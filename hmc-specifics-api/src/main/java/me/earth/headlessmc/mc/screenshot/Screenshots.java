package me.earth.headlessmc.mc.screenshot;

import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.SpecificProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes screenshots with the {@link ScreenshotProvider} of a {@link Minecraft} instance.
 * Screenshots need a game that actually renders, e.g. a normal window or a virtual display like Xvfb.
 * In HeadlessMc's headless mode all LWJGL calls are redirected and nothing is rendered,
 * so screenshots are not available, see {@link #isAvailable()}.
 */
public final class Screenshots {
    private static final Logger LOGGER = Logger.getLogger(Screenshots.class.getName());
    /**
     * Classes that HeadlessMc's LWJGL redirection, which makes the game headless, consists of.
     */
    private static final String[] HEADLESS_CLASSES = {
        "io.github.headlesshq.headlessmc.lwjgl.api.RedirectionApi",
        "me.earth.headlessmc.lwjgl.api.RedirectionApi"
    };

    private static volatile Boolean headless;

    private Screenshots() {
        throw new AssertionError();
    }

    /**
     * Screenshots are available, unless the game runs in HeadlessMc's headless mode.
     * The detection can be overridden with the system property {@value SpecificProperties#SCREENSHOTS_ENABLED}.
     *
     * @return {@code true} if screenshots can be taken.
     */
    public static boolean isAvailable() {
        String enabled = System.getProperty(SpecificProperties.SCREENSHOTS_ENABLED);
        if (enabled != null && !enabled.trim().isEmpty()) {
            return Boolean.parseBoolean(enabled.trim());
        }

        return !isHeadless();
    }

    /**
     * @return {@code true} if the LWJGL calls of the game are redirected by HeadlessMc, so that nothing is rendered.
     */
    public static boolean isHeadless() {
        Boolean result = headless;
        if (result == null) {
            result = false;
            for (String name : HEADLESS_CLASSES) {
                if (isPresent(name)) {
                    result = true;
                    break;
                }
            }

            headless = result;
        }

        return result;
    }

    /**
     * Takes a screenshot and writes it to the given file as a PNG.
     * Can be called from any thread, the screenshot is taken on Minecraft's main thread.
     *
     * @param mc the Minecraft instance.
     * @param file the file to write to, parent directories are created.
     * @return a future that completes with the file once it has been written.
     */
    public static CompletableFuture<File> take(Minecraft mc, File file) {
        CompletableFuture<File> future = new CompletableFuture<>();
        if (!isAvailable()) {
            future.completeExceptionally(new UnsupportedOperationException(
                "Screenshots are not available, because the game does not render (HeadlessMc's headless mode). "
                    + "Run the game with a display, e.g. Xvfb, or set -D" + SpecificProperties.SCREENSHOTS_ENABLED + "=true."));
            return future;
        }

        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            future.completeExceptionally(new IOException("Failed to create directory " + parent));
            return future;
        }

        ScreenshotProvider provider = mc.getScreenshotProvider();
        try {
            mc.schedule(() -> {
                try {
                    provider.takeScreenshot(file, throwable -> {
                        if (throwable == null) {
                            future.complete(file);
                        } else {
                            future.completeExceptionally(throwable);
                        }
                    });
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    /**
     * Takes a screenshot and returns it as a PNG.
     * Can be called from any thread, the screenshot is taken on Minecraft's main thread.
     *
     * @param mc the Minecraft instance.
     * @return a future that completes with the bytes of the PNG.
     */
    public static CompletableFuture<byte[]> take(Minecraft mc) {
        File file;
        try {
            file = File.createTempFile("hmc-screenshot-", ".png");
        } catch (IOException e) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }

        return take(mc, file)
            .thenApply(f -> {
                try {
                    return Files.readAllBytes(f.toPath());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read screenshot " + f, e);
                }
            })
            .whenComplete((bytes, throwable) -> {
                if (!file.delete() && file.exists()) {
                    LOGGER.log(Level.WARNING, "Failed to delete " + file);
                }
            });
    }

    private static boolean isPresent(String name) {
        ClassLoader[] classLoaders = {
            Screenshots.class.getClassLoader(), Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader classLoader : classLoaders) {
            try {
                Class.forName(name, false, classLoader);
                return true;
            } catch (ClassNotFoundException | LinkageError ignored) {
                // not present on this ClassLoader
            }
        }

        return false;
    }

}
