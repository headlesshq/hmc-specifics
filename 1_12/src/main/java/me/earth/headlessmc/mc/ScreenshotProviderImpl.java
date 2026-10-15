package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.screenshot.ScreenshotEncoder;
import me.earth.headlessmc.mc.screenshot.ScreenshotProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

/**
 * Reads the pixels with ScreenShotHelper, but encodes the PNG on another thread.
 */
public final class ScreenshotProviderImpl implements ScreenshotProvider {
    public static final ScreenshotProviderImpl INSTANCE = new ScreenshotProviderImpl();

    private ScreenshotProviderImpl() {
    }

    @Override
    public void takeScreenshot(File file, Consumer<Throwable> callback) {
        BufferedImage image;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            image = ScreenShotHelper.createScreenshot(mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        } catch (Throwable t) {
            callback.accept(t);
            return;
        }

        ScreenshotEncoder.writeAsync(image, file, callback);
    }

}
