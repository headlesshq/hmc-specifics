package me.earth.headlessmc.mc.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InputConstants.Key.class)
public interface IInputConstantsKey {
    @Accessor("NAME_MAP")
    static Map<String, InputConstants.Key> getNAME_MAP() {
        throw new IllegalStateException("NAME_MAP not mixed in!");
    }

}
