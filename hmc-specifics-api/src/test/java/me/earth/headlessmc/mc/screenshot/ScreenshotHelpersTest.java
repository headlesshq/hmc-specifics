package me.earth.headlessmc.mc.screenshot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenshotHelpersTest {
    @Test
    public void testGrabbedScreenshotMovesFile(@TempDir Path dir) throws Exception {
        File target = dir.resolve("target.png").toFile();
        CompletableFuture<Throwable> result = new CompletableFuture<>();
        GrabbedScreenshot grabbed = GrabbedScreenshot.create(target, result::complete);
        // what Minecraft does
        File screenshots = new File(grabbed.getDirectory(), "screenshots");
        assertTrue(screenshots.mkdirs());
        Files.write(new File(screenshots, grabbed.getName()).toPath(), new byte[]{1, 2, 3});
        grabbed.onGrabbed("Saved screenshot as " + grabbed.getName());

        assertNull(result.get(5, TimeUnit.SECONDS));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(target.toPath()));
        assertFalse(grabbed.getDirectory().exists());
    }

    @Test
    public void testGrabbedScreenshotFailure(@TempDir Path dir) throws Exception {
        CompletableFuture<Throwable> result = new CompletableFuture<>();
        GrabbedScreenshot grabbed = GrabbedScreenshot.create(dir.resolve("target.png").toFile(), result::complete);
        grabbed.onGrabbed("Couldn't save screenshot: Test");

        Throwable throwable = result.get(5, TimeUnit.SECONDS);
        assertInstanceOf(IOException.class, throwable);
        assertTrue(throwable.getMessage().contains("Couldn't save screenshot: Test"));
        assertFalse(grabbed.getDirectory().exists());
    }

    @Test
    public void testEncoder(@TempDir Path dir) throws Exception {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 2, 0xFF0000);
        File file = dir.resolve("encoded.png").toFile();
        CompletableFuture<Throwable> result = new CompletableFuture<>();
        ScreenshotEncoder.writeAsync(image, file, result::complete);

        assertNull(result.get(5, TimeUnit.SECONDS));
        BufferedImage read = ImageIO.read(file);
        assertEquals(4, read.getWidth());
        assertEquals(0xFF0000, read.getRGB(1, 2) & 0xFFFFFF);
    }

}
