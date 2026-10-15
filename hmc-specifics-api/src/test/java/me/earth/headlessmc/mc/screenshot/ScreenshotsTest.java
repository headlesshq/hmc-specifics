package me.earth.headlessmc.mc.screenshot;

import me.earth.headlessmc.mc.MockMc;
import me.earth.headlessmc.mc.SpecificProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenshotsTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G'};

    @AfterEach
    public void reset() {
        MockMc.INSTANCE.screenshotProvider = ScreenshotProvider.UNSUPPORTED;
        System.clearProperty(SpecificProperties.SCREENSHOTS_ENABLED);
    }

    @Test
    public void testNotHeadlessInTests() {
        assertFalse(Screenshots.isHeadless());
        assertTrue(Screenshots.isAvailable());
    }

    @Test
    public void testTakeBytes() throws Exception {
        MockMc.INSTANCE.screenshotProvider = (file, callback) -> {
            try {
                Files.write(file.toPath(), PNG);
                callback.accept(null);
            } catch (IOException e) {
                callback.accept(e);
            }
        };

        assertArrayEquals(PNG, Screenshots.take(MockMc.INSTANCE).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testTakeFileCreatesParent(@TempDir Path dir) throws Exception {
        MockMc.INSTANCE.screenshotProvider = (file, callback) -> {
            try {
                Files.write(file.toPath(), PNG);
                callback.accept(null);
            } catch (IOException e) {
                callback.accept(e);
            }
        };

        File file = dir.resolve("sub").resolve("screenshot.png").toFile();
        assertEquals(file, Screenshots.take(MockMc.INSTANCE, file).get(5, TimeUnit.SECONDS));
        assertArrayEquals(PNG, Files.readAllBytes(file.toPath()));
    }

    @Test
    public void testUnsupported() {
        ExecutionException e = assertThrows(ExecutionException.class, () -> Screenshots.take(MockMc.INSTANCE).get(5, TimeUnit.SECONDS));
        assertInstanceOf(UnsupportedOperationException.class, e.getCause());
    }

    @Test
    public void testProviderThrows() {
        MockMc.INSTANCE.screenshotProvider = (file, callback) -> {
            throw new IllegalStateException("Test");
        };

        ExecutionException e = assertThrows(ExecutionException.class, () -> Screenshots.take(MockMc.INSTANCE).get(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    public void testDisabledByProperty() {
        System.setProperty(SpecificProperties.SCREENSHOTS_ENABLED, "false");
        assertFalse(Screenshots.isAvailable());
        MockMc.INSTANCE.screenshotProvider = (file, callback) -> fail("Must not be called");
        ExecutionException e = assertThrows(ExecutionException.class, () -> Screenshots.take(MockMc.INSTANCE).get(5, TimeUnit.SECONDS));
        assertInstanceOf(UnsupportedOperationException.class, e.getCause());
    }

}
