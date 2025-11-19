package me.earth.headlessmc.mc.keyboard;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused") // used by McKeyHandler in 1.7.10 - 1.12.2
public class LegacyKeyboard extends AbstractKeyboard implements Keyboard {
    private final List<Key> keys;

    private int eventKey = 0;
    private boolean eventKeyState = false;

    public LegacyKeyboard(List<Key> keys) {
        this.keys = keys;
    }

    public boolean getEventKeyState() {
        return eventKeyState;
    }

    public int getEventKey() {
        return eventKey;
    }

    @Override
    protected void performKeyPress(Key key) {
        eventKey = key.getId();
        eventKeyState = true;
    }

    @Override
    protected void performKeyRelease(Key key) {
        eventKey = key.getId();
        eventKeyState = false;
    }


    @Override
    public @NotNull Iterator<Key> iterator() {
        return keys.iterator();
    }

}
