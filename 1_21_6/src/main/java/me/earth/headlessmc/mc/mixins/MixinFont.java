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
    @Inject(
        method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
        at = @At("HEAD"))
    private void prepareTextHook(String string, float x, float y, int color, boolean shadow, int backgroundColor,
                                 CallbackInfoReturnable<Font.PreparedText> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(string, x, y);
        }
    }

    @Inject(
            method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            at = @At("HEAD"))
    private void prepareTextHook1(FormattedCharSequence sequence, float x, float y, int color, boolean shadow,
                                  int backgroundColor, CallbackInfoReturnable<Font.PreparedText> cir) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(sequence), x, y);
        }
    }

    @Inject(
            method = "drawInBatch8xOutline",
            at = @At("HEAD"))
    private void drawInBatch8xOutlineHook(FormattedCharSequence sequence, float x, float y, int color,
                                          int backgroundColor, Matrix4f matrix, MultiBufferSource bufferSource,
                                          int light, CallbackInfo ci) {
        if (FontRendererImpl.INSTANCE.hasListeners()) {
            FontRendererImpl.INSTANCE.onRender(CharSinkUtil.toString(sequence), x, y);
        }
    }

}
