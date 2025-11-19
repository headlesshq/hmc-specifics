package me.earth.headlessmc.mc;

import com.mojang.blaze3d.platform.InputConstants;
import me.earth.headlessmc.mc.keyboard.AbstractKeyboard;
import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.mixins.IInputConstantsKey;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.stream.Collectors;

public class KeyboardImpl extends AbstractKeyboard implements Keyboard {
    private final Minecraft mc;

    public KeyboardImpl(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public void performKeyPress(Key key) {
        mc.keyboardHandler.keyPress(
                mc.getWindow().getWindow(),
                key.getId(),
                key.getScanCode(),
                GLFW.GLFW_PRESS,
                0);
    }

    @Override
    public void performKeyRelease(Key key) {
        mc.keyboardHandler.keyPress(
                mc.getWindow().getWindow(),
                key.getId(),
                key.getScanCode(),
                GLFW.GLFW_RELEASE,
                0);
    }

    @Override
    public @NotNull Iterator<Key> iterator() {
        return IInputConstantsKey
                .getNAME_MAP()
                .values()
                .stream()
                .map(k -> Key.createFromMinecraftName(
                        k.getName(),
                        k.getType() == InputConstants.Type.SCANCODE ? -1 : k.getValue(),
                        k.getType() == InputConstants.Type.SCANCODE ? k.getValue() : -1))
                .sorted()
                .collect(Collectors.toList())
                .iterator();
    }

}
