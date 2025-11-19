package me.earth.headlessmc.mc.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import me.earth.headlessmc.mc.keyboard.HmcKeyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class MixinInputConstants {
    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void isKeyDownHook(@SuppressWarnings("unused") long windowId, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (HmcKeyboard.getInstance().isKeyDown(keyCode)) {
            cir.setReturnValue(true);
        }
    }

}
