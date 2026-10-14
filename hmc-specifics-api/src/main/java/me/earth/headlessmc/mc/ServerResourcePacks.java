package me.earth.headlessmc.mc;

/**
 * Lets the version specific mixins skip the "Use server resource pack?" prompt.
 * Enabled by launching the game with {@code -Dhmc.resourcepacks.autoaccept=true}.
 */
public final class ServerResourcePacks {
    private ServerResourcePacks() {
        throw new AssertionError();
    }

    public static boolean isAutoAcceptEnabled() {
        return SpecificProperties.getBoolean(SpecificProperties.AUTO_ACCEPT_SERVER_RESOURCE_PACKS, false);
    }

}
