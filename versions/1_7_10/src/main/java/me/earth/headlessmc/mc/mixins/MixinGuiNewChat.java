package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ComponentHelper;
import me.earth.headlessmc.mc.adventure.AdventureWrapper;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {
    @Shadow
    @Final
    private static Logger logger;

    @Inject(method = "printChatMessageWithOptionalDeletion",
        at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"), cancellable = true)
    private void logChatMessageHook(IChatComponent content, int id, CallbackInfo ci) {
        if (AdventureWrapper.ENABLED) {
            String ansiString = ComponentHelper.toAnsiString(content);
            if (ansiString != null) {
                //noinspection StringConcatenationArgumentToLogCall
                logger.info("[CHAT] " + ansiString);
                ci.cancel();
            }
        }
    }

}
