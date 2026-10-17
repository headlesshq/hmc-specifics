package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ComponentHelper;
import me.earth.headlessmc.mc.adventure.AdventureWrapper;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class MixinChatComponent {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;I)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"), cancellable = true)
    private void logChatMessageHook(Component content, int id, CallbackInfo ci) {
        if (AdventureWrapper.ENABLED) {
            String ansiString = ComponentHelper.toAnsiString(content);
            if (ansiString != null) {
                LOGGER.info("[CHAT] {}", ansiString);
                ci.cancel();
            }
        }
    }

}
