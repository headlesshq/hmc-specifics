package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.screenshot.ScreenshotEncoder;
import me.earth.headlessmc.mc.screenshot.ScreenshotProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.util.function.Consumer;

/**
 * Reads the pixels like ScreenShotHelper.saveScreenshot, but encodes the PNG on another thread.
 */
public final class ScreenshotProviderImpl implements ScreenshotProvider {
    public static final ScreenshotProviderImpl INSTANCE = new ScreenshotProviderImpl();

    private ScreenshotProviderImpl() {
    }

    @Override
    public void takeScreenshot(File file, Consumer<Throwable> callback) {
        BufferedImage image;
        try {
            image = readPixels();
        } catch (Throwable t) {
            callback.accept(t);
            return;
        }

        ScreenshotEncoder.writeAsync(image, file, callback);
    }

    private static BufferedImage readPixels() {
        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer framebuffer = mc.getFramebuffer();
        boolean framebufferEnabled = OpenGlHelper.isFramebufferEnabled();
        int width = framebufferEnabled ? framebuffer.framebufferTextureWidth : mc.displayWidth;
        int height = framebufferEnabled ? framebuffer.framebufferTextureHeight : mc.displayHeight;
        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(width * height);
        int[] pixelValues = new int[width * height];
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        if (framebufferEnabled) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
        } else {
            GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
        }

        pixelBuffer.get(pixelValues);
        // flips the image vertically
        TextureUtil.func_147953_a(pixelValues, width, height);
        if (!framebufferEnabled) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixelValues, 0, width);
            return image;
        }

        BufferedImage image = new BufferedImage(framebuffer.framebufferWidth, framebuffer.framebufferHeight, BufferedImage.TYPE_INT_RGB);
        int offset = framebuffer.framebufferTextureHeight - framebuffer.framebufferHeight;
        for (int y = offset; y < framebuffer.framebufferTextureHeight; y++) {
            for (int x = 0; x < framebuffer.framebufferWidth; x++) {
                image.setRGB(x, y - offset, pixelValues[y * framebuffer.framebufferTextureWidth + x]);
            }
        }

        return image;
    }

}
