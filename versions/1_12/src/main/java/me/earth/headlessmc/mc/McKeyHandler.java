package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.LegacyKeyboard;
import me.earth.headlessmc.mc.mixins.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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

        // no debug crash

        dispatchKeypresses(keyboard);
        if (mc.currentScreen != null) {
            mc.currentScreen.handleKeyboardInput();
        }

        boolean pressed = keyboard.getEventKeyState();
        if (pressed) {
            if (key == 62 && mc.entityRenderer != null) {
                mc.entityRenderer.switchUseShader();
            }

            boolean f3 = false;
            if (mc.currentScreen == null) {
                if (key == 1) {
                    mc.displayInGameMenu();
                }

                f3 = keyboard.isKeyDown(61) && processKeyF3(key);
                ((IMinecraft) mc).setActionKeyF3(((IMinecraft) mc).getActionKeyF3() || f3);
                if (key == 59) {
                    mc.gameSettings.hideGUI = !mc.gameSettings.hideGUI;
                }
            }

            if (f3) {
                KeyBinding.setKeyBindState(key, false);
            } else {
                KeyBinding.setKeyBindState(key, true);
                KeyBinding.onTick(key);
            }

            // no debug Profiler name
        } else {
            KeyBinding.setKeyBindState(key, false);
            if (key == 61) {
                if (((IMinecraft) mc).getActionKeyF3()) {
                    ((IMinecraft) mc).setActionKeyF3(false);
                } else {
                    mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
                    mc.gameSettings.showDebugProfilerChart = mc.gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
                    mc.gameSettings.showLagometer = mc.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
                }
            }
        }
    }

    public static void dispatchKeypresses(LegacyKeyboard keyboard) {
        Minecraft mc = Minecraft.getMinecraft();
        int key = keyboard.getEventKey();
        if (key != 0) {
            if (!(mc.currentScreen instanceof GuiControls) || ((GuiControls)mc.currentScreen).time <= Minecraft.getSystemTime() - 20L) {
                if (keyboard.getEventKeyState()) {
                    if (key == mc.gameSettings.keyBindFullscreen.getKeyCode()) {
                        mc.toggleFullscreen();
                    } else if (key == mc.gameSettings.keyBindScreenshot.getKeyCode()) {
                        mc.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(mc.gameDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer()));
                    } else if (key == 48 && GuiScreen.isCtrlKeyDown() && (mc.currentScreen == null || !mc.currentScreen.isFocused())) {
                        mc.gameSettings.setOptionValue(GameSettings.Options.NARRATOR, 1);
                        if (mc.currentScreen instanceof ScreenChatOptions) {
                            ((ScreenChatOptions)mc.currentScreen).updateNarratorButton();
                        }
                    }
                }

            }
        }
    }

    private static boolean processKeyF3(int auxKey) {
        Minecraft mc = Minecraft.getMinecraft();
        if (auxKey == 30) {
            mc.renderGlobal.loadRenderers();
            debugFeedbackTranslated("debug.reload_chunks.message");
            return true;
        } else if (auxKey == 48) {
            boolean bl = !mc.getRenderManager().isDebugBoundingBox();
            mc.getRenderManager().setDebugBoundingBox(bl);
            debugFeedbackTranslated(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
            return true;
        } else if (auxKey == 32) {
            if (mc.ingameGUI != null) {
                mc.ingameGUI.getChatGUI().clearChatMessages(false);
            }

            return true;
        } else if (auxKey == 33) {
            mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
            debugFeedbackTranslated("debug.cycle_renderdistance.message", mc.gameSettings.renderDistanceChunks);
            return true;
        } else if (auxKey == 34) {
            boolean bl = mc.debugRenderer.toggleChunkBorders();
            debugFeedbackTranslated(bl ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
            return true;
        } else if (auxKey == 35) {
            mc.gameSettings.advancedItemTooltips = !mc.gameSettings.advancedItemTooltips;
            debugFeedbackTranslated(mc.gameSettings.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
            mc.gameSettings.saveOptions();
            return true;
        } else if (auxKey == 49) {
            if (!mc.player.canUseCommand(2, "")) {
                debugFeedbackTranslated("debug.creative_spectator.error");
            } else if (mc.player.isCreative()) {
                mc.player.sendChatMessage("/gamemode spectator");
            } else if (mc.player.isSpectator()) {
                mc.player.sendChatMessage("/gamemode creative");
            }

            return true;
        } else if (auxKey == 25) {
            mc.gameSettings.pauseOnLostFocus = !mc.gameSettings.pauseOnLostFocus;
            mc.gameSettings.saveOptions();
            debugFeedbackTranslated(mc.gameSettings.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
            return true;
        } else if (auxKey == 16) {
            debugFeedbackTranslated("debug.help.message");
            GuiNewChat guiNewChat = mc.ingameGUI.getChatGUI();
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.reload_chunks.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.show_hitboxes.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.clear_chat.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.cycle_renderdistance.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.chunk_boundaries.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.advanced_tooltips.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.creative_spectator.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.pause_focus.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.help.help", new Object[0]));
            guiNewChat.printChatMessage(new TextComponentTranslation("debug.reload_resourcepacks.help", new Object[0]));
            return true;
        } else if (auxKey == 20) {
            debugFeedbackTranslated("debug.reload_resourcepacks.message");
            mc.refreshResources();
            return true;
        } else {
            return false;
        }
    }

    private static void debugFeedbackTranslated(String string, Object... objs) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((new TextComponentString("")).appendSibling((new TextComponentTranslation("debug.prefix", new Object[0])).setStyle((new Style()).setColor(TextFormatting.YELLOW).setBold(true))).appendText(" ").appendSibling(new TextComponentTranslation(string, objs)));
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
