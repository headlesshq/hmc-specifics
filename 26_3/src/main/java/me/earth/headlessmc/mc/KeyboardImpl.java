package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.keyboard.AbstractKeyboard;
import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.mixins.IInputConstantsKey;
import me.earth.headlessmc.mc.mixins.IKeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class KeyboardImpl extends AbstractKeyboard implements Keyboard {
    // GLFW is no longer on the classpath, these are the GLFW action values
    private static final int ACTION_RELEASE = 0;
    private static final int ACTION_PRESS = 1;

    private final Minecraft mc;

    public KeyboardImpl(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public void performKeyPress(Key key) {
        ((IKeyboardHandler) mc.keyboardHandler).keyPress(
                mc.getWindow().handle(),
                ACTION_PRESS,
                new KeyEvent(key.getId(), key.getScanCode(), 0)
        );
    }

    @Override
    public void performKeyRelease(Key key) {
        ((IKeyboardHandler) mc.keyboardHandler).keyPress(
                mc.getWindow().handle(),
                ACTION_RELEASE,
                new KeyEvent(key.getId(), key.getScanCode(), 0)
        );
    }

    @Override
    public @NotNull Iterator<Key> iterator() {
        return IInputConstantsKey
                .getNAME_MAP()
                .values()
                .stream()
                .map(k -> Key.createFromMinecraftName(
                        k.getName(),
                        k.getValue(),
                        -1))
                .sorted()
                .toList()
                .iterator();
    }

}
