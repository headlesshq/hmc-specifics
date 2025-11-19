package me.earth.headlessmc.mc.keyboard;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// seems like on new mc versions java.awt.headless=true, it's set in the main class, see 1.21.5 main
public class RobotKeyboard extends AbstractKeyboard implements Keyboard {
    @Override
    public void performKeyPress(Key key) {
        try {
            Robot robot = new Robot();
            robot.keyPress(key.getId());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void performKeyRelease(Key key) {
        try {
            Robot robot = new Robot();
            robot.keyRelease(key.getId());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public @NotNull Iterator<Key> iterator() {
        List<Key> keys = new ArrayList<>();
        for (Field field : KeyEvent.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getName().startsWith("VK_")
                    && int.class == field.getType()) {
                String name = field.getName().substring(3);
                try {
                    int key = (int) field.get(null);
                    keys.add(new Key(name, key, -1));
                } catch (IllegalAccessException e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        return keys.iterator();
    }

}
