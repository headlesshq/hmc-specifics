package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.CharSinkUtil;
import me.earth.headlessmc.mc.FontRendererImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.Font.class)
public abstract class MixinFont {
    // for some reason these do not work when running 1.21.5 in my IDE
    // EVEN WITH require = 0 wtf

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
