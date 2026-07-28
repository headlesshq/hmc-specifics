package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.CharSinkUtil;
import me.earth.headlessmc.mc.FontRendererImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.Font.class)
public abstract class MixinFont {
    // for some reason these do not work when running 1.21.5 in my IDE
    // EVEN WITH require = 0 wtf
    @Inject(
        method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
        at = @At("HEAD"))
    private void renderTextHook0(String str, float x, float y, int color, boolean dropShadow, Matrix4fc pose, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLightCoords, CallbackInfo ci) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(str, x, y);
        }
    }

    @Inject(
            method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"))
    private void renderTextHook0(Component str, float x, float y, int color, boolean dropShadow, Matrix4fc pose, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLightCoords, CallbackInfo ci) {
        FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(str.getVisualOrderText()/* TODO: is this right?*/), x, y);
    }

    @Inject(
            method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"))
    private void renderTextHook1(FormattedCharSequence str, float x, float y, int color, boolean dropShadow, Matrix4fc pose, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLightCoords, CallbackInfo ci) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(str), x, y);
        }
    }

    @Inject(method = "drawInBatch8xOutline", at = @At("HEAD"))
    private void drawInBatch8xOutlineHook(FormattedCharSequence str, float x, float y, int color, int outlineColor, Matrix4fc pose, MultiBufferSource bufferSource, int packedLightCoords, CallbackInfo ci) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(str), x, y);
        }
    }

    @Inject(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"))
    private void prepareTextHook(String text, float x, float y, int originalColor, boolean drawShadow, int backgroundColor, CallbackInfoReturnable<Font.PreparedText> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(text, x, y);
        }
    }

    @Inject(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"))
    private void prepareTextHook0(FormattedCharSequence text, float x, float y, int originalColor, boolean drawShadow, boolean includeEmpty, int backgroundColor, CallbackInfoReturnable<Font.PreparedText> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(text), x, y);
        }
    }

}
