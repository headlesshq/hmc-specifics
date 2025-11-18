package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.CharSinkUtil;
import me.earth.headlessmc.mc.FontRendererImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
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
            method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"))
    private void renderTextHook0(FormattedCharSequence sequence,
                                 float x, float y, int color,
                                 boolean dropShadow,
                                 Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 Font.DisplayMode displayMode, int j, int k,
                                 CallbackInfoReturnable<Float> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(
                    CharSinkUtil.toString(sequence), x, y);
        }
    }

    @Inject(
            method = "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"))
    private void renderTextHook0(String string, float x, float y, int color,
                                 boolean dropShadow, Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 Font.DisplayMode displayMode, int j, int k,
                                 CallbackInfoReturnable<Float> cir) {
        FontRendererImpl.INSTANCE.onRender(string, x, y);
    }

    @Inject(method = "drawInBatch8xOutline", at = @At("HEAD"))
    private void drawInBatch8xOutlineHook(FormattedCharSequence sequence,
                                          float x, float y, int i, int j,
                                          Matrix4f matrix4f,
                                          MultiBufferSource multiBufferSource,
                                          int k, CallbackInfo ci) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(
                    CharSinkUtil.toString(sequence), x, y);
        }
    }

    // for some reason i need these when running 1.21.5 in my IDE
    @Inject(
            method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)F",
            at = @At("HEAD"))
    private void renderTextHook1(FormattedCharSequence sequence,
                                 float x, float y, int color,
                                 boolean dropShadow,
                                 Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 Font.DisplayMode displayMode, int j, int k, boolean inverseDepth,
                                 CallbackInfoReturnable<Float> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(
                    CharSinkUtil.toString(sequence), x, y);
        }
    }

    @Inject(
            method = "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)F",
            at = @At("HEAD"))
    private void renderTextHook1(String string, float x, float y, int color,
                                 boolean dropShadow, Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 Font.DisplayMode displayMode, int j, int k, boolean inverseDepth,
                                 CallbackInfoReturnable<Float> cir) {
        FontRendererImpl.INSTANCE.onRender(string, x, y);
    }

}