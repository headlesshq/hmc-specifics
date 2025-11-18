package me.earth.headlessmc.mc.keyboard;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractKeyboard implements Keyboard {
    private final Set<Key> pressed = Collections.newSetFromMap(new ConcurrentHashMap<>());

    protected abstract void performKeyPress(Key key);

    protected abstract void performKeyRelease(Key key);

    @Override
    public synchronized boolean isKeyDown(Key key) {
        return pressed.contains(key);
    }

    @Override
    public synchronized void press(Key key) {
        pressed.add(key);
        performKeyPress(key);
    }

    @Override
    public synchronized void release(Key key) {
        pressed.remove(key);
        performKeyRelease(key);
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        return pressed.stream().anyMatch(key -> key.getId() == keyCode || key.getScanCode() == keyCode);
    }

}
