package me.earth.headlessmc.mc;

import me.earth.headlessmc.mc.adventure.AdventureHelper;
import me.earth.headlessmc.mc.adventure.AdventureWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ComponentHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean adventureHelperInitialized = false;
    private static AdventureHelper adventureHelper;

    public static String toAnsiStringOrContent(ITextComponent component) {
        String string = toAnsiString(component);
        return string == null ? component.getUnformattedText() : string;
    }

    public static String toAnsiString(ITextComponent component) {
        return toAnsiString(component.getFormattedText(), component.getUnformattedText());
    }

    public static String toAnsiString(String legacyString, String unformatted) {
        if (AdventureWrapper.ENABLED) {
            String ansiString = null;
            try {
                if (!adventureHelperInitialized) {
                    adventureHelper = AdventureWrapper.getAdventureHelper(I18n::hasKey, (s, def) -> I18n.format(s));
                    adventureHelperInitialized = true;
                }

                if (adventureHelper != null) {
                    // OK FOR SOME REASON IT DOESNT WORK IN 1.12.2????????? Potentially due to JLine?
                    // I have no clue but I do not care for legacy enough rn
                    ansiString = adventureHelper.toAnsiStringLegacy(legacyString);
                }
            } catch (Exception e) {
                if (AdventureWrapper.OUTPUT_THROWABLES) {
                    if (e.getMessage().contains("Action not allowed: ")
                            || e.getMessage().contains("type not allowed: ")
                            || e.getMessage().contains("Can't access registry ")) {
                        ansiString = unformatted;
                    } else {
                        LOGGER.error("Failed to serialize {}", unformatted, e);
                    }
                }
            }

            return ansiString;
        }

        return unformatted;
    }

}
