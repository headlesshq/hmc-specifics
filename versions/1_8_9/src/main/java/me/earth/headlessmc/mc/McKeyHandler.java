package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.LegacyKeyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
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
        int key = keyboard.getEventKey();
        KeyBinding.setKeyBindState(key, keyboard.getEventKeyState());
        if (keyboard.getEventKeyState()) {
            KeyBinding.onTick(key);
        }

        // no debugCrashKeyPressTime

        dispatchKeypresses(keyboard);
        if (keyboard.getEventKeyState()) {
            if (key == 62 && mc.entityRenderer != null) {
                mc.entityRenderer.switchUseShader();
            }

            if (mc.currentScreen != null) {
                mc.currentScreen.handleKeyboardInput();
            } else {
                if (key == 1) {
                    mc.displayInGameMenu();
                }

                if (key == 32 && keyboard.isKeyDown(61) && mc.ingameGUI != null) {
                    mc.ingameGUI.getChatGUI().clearChatMessages();
                }

                if (key == 31 && keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (key == 17 && keyboard.isKeyDown(61)) {
                }

                if (key == 18 && keyboard.isKeyDown(61)) {
                }

                if (key == 47 && keyboard.isKeyDown(61)) {
                }

                if (key == 38 && keyboard.isKeyDown(61)) {
                }

                if (key == 22 && keyboard.isKeyDown(61)) {
                }

                if (key == 20 && keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (key == 33 && keyboard.isKeyDown(61)) {
                    mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                }

                if (key == 30 && keyboard.isKeyDown(61)) {
                    mc.renderGlobal.loadRenderers();
                }

                if (key == 35 && keyboard.isKeyDown(61)) {
                    mc.gameSettings.advancedItemTooltips = !mc.gameSettings.advancedItemTooltips;
                    mc.gameSettings.saveOptions();
                }

                if (key == 48 && keyboard.isKeyDown(61)) {
                    mc.getRenderManager().setDebugBoundingBox(!mc.getRenderManager().isDebugBoundingBox());
                }

                if (key == 25 && keyboard.isKeyDown(61)) {
                    mc.gameSettings.pauseOnLostFocus = !mc.gameSettings.pauseOnLostFocus;
                    mc.gameSettings.saveOptions();
                }

                if (key == 59) {
                    mc.gameSettings.hideGUI = !mc.gameSettings.hideGUI;
                }

                if (key == 61) {
                    mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
                    mc.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                    mc.gameSettings.showLagometer = GuiScreen.isAltKeyDown();
                }

                if (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
                    ++mc.gameSettings.thirdPersonView;
                    if (mc.gameSettings.thirdPersonView > 2) {
                        mc.gameSettings.thirdPersonView = 0;
                    }

                    if (mc.gameSettings.thirdPersonView == 0) {
                        mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
                    } else if (mc.gameSettings.thirdPersonView == 1) {
                        mc.entityRenderer.loadEntityShader((Entity)null);
                    }

                    mc.renderGlobal.setDisplayListEntitiesDirty();
                }

                if (mc.gameSettings.keyBindSmoothCamera.isPressed()) {
                    mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera;
                }
            }

            // no updateDebugProfiler
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
        for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++) {
            String keyName = Keyboard.getKeyName(i);
            if (keyName == null || keyName.isEmpty()) {
                keyName = "Key." + i;
            }

            keys.add(new Key(keyName, i, -1));
        }

        return keys;
    }

}
