package me.earth.headlessmc.mc.keyboard;

import me.earth.headlessmc.mc.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HmcKeyboard implements Keyboard {
    private static final HmcKeyboard INSTANCE = new HmcKeyboard();

    private final Set<Keyboard> keyboards = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile Keyboard mcKeyboard;

    public synchronized Keyboard getMcKeyboard(Minecraft mc) {
        if (mcKeyboard == null) {
            mcKeyboard = mc.getKeyboard();
            keyboards.add(mcKeyboard);
        }

        return mcKeyboard;
    }

    @Override
    public synchronized boolean isKeyDown(Key key) {
        return keyboards.stream().anyMatch(keyboard -> keyboard.isKeyDown(key));
    }

    @Override
    public synchronized boolean isKeyDown(int key) {
        return keyboards.stream().anyMatch(keyboard -> keyboard.isKeyDown(key));
    }

    @Override
    public synchronized void press(Key key) {
        if (mcKeyboard != null) {
            mcKeyboard.press(key);
        }
    }

    @Override
    public synchronized void release(Key key) {
        if (mcKeyboard != null) {
            mcKeyboard.release(key);
        }
    }

    @Override
    public synchronized @NotNull Iterator<Key> iterator() {
        return mcKeyboard != null ? mcKeyboard.iterator() : Collections.emptyIterator();
    }

    public static HmcKeyboard getInstance() {
        return INSTANCE;
    }

}
