package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.screenshot.GrabbedScreenshot;
import me.earth.headlessmc.mc.screenshot.ScreenshotProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Lets Minecraft grab the screenshot, it encodes the PNG on its IO pool.
 */
public final class ScreenshotProviderImpl implements ScreenshotProvider {
    public static final ScreenshotProviderImpl INSTANCE = new ScreenshotProviderImpl();

    private ScreenshotProviderImpl() {
    }

    @Override
    public void takeScreenshot(File file, Consumer<Throwable> callback) {
        GrabbedScreenshot grabbed;
        try {
            grabbed = GrabbedScreenshot.create(file, callback);
        } catch (IOException e) {
            callback.accept(e);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(grabbed.getDirectory(), grabbed.getName(), mc.getMainRenderTarget(), component -> grabbed.onGrabbed(component.getString()));
    }

}
