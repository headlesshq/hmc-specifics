package me.earth.headlessmc.mc.mixins;

import com.mojang.math.Matrix4f;
import me.earth.headlessmc.mc.CharSinkUtil;
import me.earth.headlessmc.mc.FontRendererImpl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.Font.class)
public abstract class MixinFont {
    @Inject(method = "renderText(Lnet/minecraft/util/FormattedCharSequence;" +
        "FFIZLcom/mojang/math/Matrix4f;" +
        "Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("HEAD"))
    private void renderTextHook0(FormattedCharSequence sequence,
                                 float x, float y, int i, boolean bl,
                                 Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 boolean bl2, int j, int k,
                                 CallbackInfoReturnable<Float> cir) {
        FontRendererImpl.INSTANCE.onRender(
            CharSinkUtil.toString(sequence), x, y);
    }

    @Inject(method = "renderText(Ljava/lang/String;FFIZ" +
        "Lcom/mojang/math/Matrix4f;" +
        "Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("HEAD"))
    private void renderTextHook1(String string, float x, float y, int i,
                                 boolean bl, Matrix4f matrix4f,
                                 MultiBufferSource multiBufferSource,
                                 boolean bl2, int j, int k,
                                 CallbackInfoReturnable<Float> cir) {
        FontRendererImpl.INSTANCE.onRender(string, x, y);
    }

}
