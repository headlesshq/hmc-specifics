package me.earth.headlessmc.mc.screenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * For {@link ScreenshotProvider}s of versions since 1.16, whose {@code Screenshot.grab(File, String, ...)}
 * takes the screenshot on the main thread, writes it to {@code <directory>/screenshots/<name>} on Minecraft's IO pool
 * and then calls a callback with the message Minecraft would show in chat.
 * This lets Minecraft grab into a temporary directory and moves the file to its target afterwards:
 * <pre>{@code
 * GrabbedScreenshot grabbed = GrabbedScreenshot.create(file, callback);
 * Screenshot.grab(grabbed.getDirectory(), grabbed.getName(), mc.getMainRenderTarget(), c -> grabbed.onGrabbed(c.getString()));
 * }</pre>
 */
public final class GrabbedScreenshot {
    private final Consumer<Throwable> callback;
    private final File directory;
    private final File target;
    private final String name;

    private GrabbedScreenshot(File directory, String name, File target, Consumer<Throwable> callback) {
        this.directory = directory;
        this.name = name;
        this.target = target;
        this.callback = callback;
    }

    /**
     * @param target the file the screenshot should be written to.
     * @param callback the callback of {@link ScreenshotProvider#takeScreenshot(File, Consumer)}.
     * @return a new GrabbedScreenshot with a temporary directory.
     * @throws IOException if the temporary directory could not be created.
     */
    public static GrabbedScreenshot create(File target, Consumer<Throwable> callback) throws IOException {
        File directory = Files.createTempDirectory("hmc-screenshot-").toFile();
        return new GrabbedScreenshot(directory, UUID.randomUUID() + ".png", target, callback);
    }

    /**
     * @return the directory to pass to Minecraft, it creates a screenshots directory inside it.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @return the name of the file to pass to Minecraft.
     */
    public String getName() {
        return name;
    }

    /**
     * Called by the callback of {@code Screenshot.grab}, after Minecraft has written the file or failed to.
     *
     * @param message the message Minecraft would show in chat, used if Minecraft failed.
     */
    public void onGrabbed(String message) {
        File screenshots = new File(directory, "screenshots");
        File written = new File(screenshots, name);
        try {
            if (written.isFile()) {
                Files.move(written.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                callback.accept(null);
            } else {
                callback.accept(new IOException("Minecraft failed to take a screenshot: " + message));
            }
        } catch (Throwable t) {
            callback.accept(t);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            written.delete();
            //noinspection ResultOfMethodCallIgnored
            screenshots.delete();
            //noinspection ResultOfMethodCallIgnored
            directory.delete();
        }
    }

}
