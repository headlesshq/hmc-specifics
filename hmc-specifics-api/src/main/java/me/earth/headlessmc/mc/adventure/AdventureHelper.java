package me.earth.headlessmc.mc.adventure;

/**
 * Takes Strings instead of gson objects, because we relocate our own gson.
 * Older versions of Minecraft ship gson versions that are too old for our dependencies.
 *
 * @see AdventureHelperImpl
 */
public interface AdventureHelper {
    String toAnsiStringJson(String jsonString);

    String toAnsiStringLegacy(String legacyString);

}
