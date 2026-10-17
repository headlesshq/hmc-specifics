package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.LegacyKeyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ScreenShotHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class McKeyHandler {
    private static final LegacyKeyboard KEYBOARD = new LegacyKeyboard(findKeys()) {
        @Override
        public void press(Key key) {
            super.press(key);
            update(this);
        }

        @Override
        public void release(Key key) {
            super.release(key);
            update(this);
        }
    };

    public static LegacyKeyboard getKeyboard() {
        return KEYBOARD;
    }

    public static void update(LegacyKeyboard keyboard) {
        Minecraft mc = Minecraft.getMinecraft();
        KeyBinding.setKeyBindState(keyboard.getEventKey(), keyboard.getEventKeyState());
        if (keyboard.getEventKeyState()) {
            KeyBinding.onTick(keyboard.getEventKey());
        }

        // no debugCrashKeyPressTime

        dispatchKeypresses(keyboard);
        if (keyboard.getEventKeyState()) {
            if (keyboard.getEventKey() == 62 && mc.entityRenderer != null) {
                mc.entityRenderer.deactivateShader();
            }

            if (mc.currentScreen != null) {
                mc.currentScreen.handleKeyboardInput();
            } else {
                if (keyboard.getEventKey() == 1) {
                    mc.displayInGameMenu();
                }

                if (keyboard.getEventKey() == 31 && keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (keyboard.getEventKey() == 20 && keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (keyboard.getEventKey() == 33 && keyboard.isKeyDown(61)) {
                    boolean var9 = keyboard.isKeyDown(42) | keyboard.isKeyDown(54);
                    // decompilation doesnt work
                    // mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, var9 ? -1 : 1);
                }

                if (keyboard.getEventKey() == 30 && keyboard.isKeyDown(61)) {
                    mc.renderGlobal.loadRenderers();
                }

                if (keyboard.getEventKey() == 35 && keyboard.isKeyDown(61)) {
                    mc.gameSettings.advancedItemTooltips = !mc.gameSettings.advancedItemTooltips;
                    mc.gameSettings.saveOptions();
                }

                if (keyboard.getEventKey() == 48 && keyboard.isKeyDown(61)) {
                    RenderManager.debugBoundingBox = !RenderManager.debugBoundingBox;
                }

                if (keyboard.getEventKey() == 25 && keyboard.isKeyDown(61)) {
                    mc.gameSettings.pauseOnLostFocus = !mc.gameSettings.pauseOnLostFocus;
                    mc.gameSettings.saveOptions();
                }

                if (keyboard.getEventKey() == 59) {
                    mc.gameSettings.hideGUI = !mc.gameSettings.hideGUI;
                }

                if (keyboard.getEventKey() == 61) {
                    mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
                    mc.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                }

                if (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
                    ++mc.gameSettings.thirdPersonView;
                    if (mc.gameSettings.thirdPersonView > 2) {
                        mc.gameSettings.thirdPersonView = 0;
                    }
                }

                if (mc.gameSettings.keyBindSmoothCamera.isPressed()) {
                    mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera;
                }
            }

            /*if (mc.gameSettings.showDebugInfo && mc.gameSettings.showDebugProfilerChart) {
                if (keyboard.getEventKey() == 11) {
                    mc.updateDebugProfilerName(0);
                }

                for(int var10 = 0; var10 < 9; ++var10) {
                    if (keyboard.getEventKey() == 2 + var10) {
                        mc.updateDebugProfilerName(var10 + 1);
                    }
                }
            }*/
        }
    }

    public static void dispatchKeypresses(LegacyKeyboard keyboard) {
        Minecraft mc = Minecraft.getMinecraft();
        if (keyboard.getEventKey() != 0) {
            if (!(mc.currentScreen instanceof GuiControls) || ((GuiControls)mc.currentScreen).time <= Minecraft.getSystemTime() - 20L) {
                if (keyboard.getEventKeyState()) {
                    if (keyboard.getEventKey() == mc.gameSettings.keyBindFullscreen.getKeyCode()) {
                        mc.toggleFullscreen();
                    } else if (keyboard.getEventKey() == mc.gameSettings.keyBindScreenshot.getKeyCode()) {
                        mc.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer()));
                    }
                }
            }
        }
    }

    private static List<Key> findKeys() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < org.lwjgl.input.Keyboard.KEYBOARD_SIZE; i++) {
            String keyName = org.lwjgl.input.Keyboard.getKeyName(i);
            if (keyName == null || keyName.isEmpty()) {
                keyName = "Key." + i;
            }

            keys.add(new Key(keyName, i, -1));
        }

        return keys;
    }

}
