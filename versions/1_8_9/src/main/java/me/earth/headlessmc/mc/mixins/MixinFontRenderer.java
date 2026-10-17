package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.FontRendererImpl;
import me.earth.headlessmc.mc.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.FontRenderer.class)
public abstract class MixinFontRenderer implements FontRenderer {
    @Inject(method = "renderString", at = @At("HEAD"))
    private void onRender(String text, float x, float y, int color,
                          boolean dropShadow,
                          CallbackInfoReturnable<Integer> cir) {
        FontRendererImpl.INSTANCE.onRender(text, x, y);
    }

}
