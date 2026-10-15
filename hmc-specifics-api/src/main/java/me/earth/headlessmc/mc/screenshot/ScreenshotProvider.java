package me.earth.headlessmc.mc.screenshot;

import java.io.File;
import java.util.function.Consumer;

/**
 * Takes screenshots of what Minecraft renders, implemented by the version specific modules.
 * Use {@link Screenshots} instead of calling this directly, it checks if the game renders at all
 * and schedules the screenshot on Minecraft's main thread.
 */
@FunctionalInterface
public interface ScreenshotProvider {
    /**
     * A ScreenshotProvider for versions that do not support screenshots.
     */
    ScreenshotProvider UNSUPPORTED = (file, callback) ->
        callback.accept(new UnsupportedOperationException("Screenshots are not supported on this version of Minecraft"));

    /**
     * Takes a screenshot of the last rendered frame and writes it to the given file as a PNG.
     * Has to be called on Minecraft's main thread. Encoding the image should happen on another thread,
     * so that the game does not stutter.
     *
     * @param file the file to write the PNG to, its parent directory exists.
     * @param callback called once the file has been written with {@code null}, or with the Throwable that occurred.
     *                 Can be called on any thread.
     */
    void takeScreenshot(File file, Consumer<Throwable> callback);

}
