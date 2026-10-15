package me.earth.headlessmc.mc.screenshot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * For {@link ScreenshotProvider}s of versions before 1.16, which read the pixels into a {@link BufferedImage}
 * on the main thread. Encoding a PNG takes a while, so it happens on another thread.
 */
public final class ScreenshotEncoder {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "HMC-Screenshot-Encoder");
        thread.setDaemon(true);
        return thread;
    });

    private ScreenshotEncoder() {
        throw new AssertionError();
    }

    /**
     * Writes the image to the file as a PNG on another thread.
     *
     * @param image the image to write.
     * @param file the file to write to.
     * @param callback called with {@code null} once the file has been written, or with the Throwable that occurred.
     */
    public static void writeAsync(BufferedImage image, File file, Consumer<Throwable> callback) {
        EXECUTOR.execute(() -> {
            try {
                if (!ImageIO.write(image, "png", file)) {
                    throw new IOException("No ImageIO writer for png");
                }

                callback.accept(null);
            } catch (Throwable t) {
                callback.accept(t);
            }
        });
    }

}
