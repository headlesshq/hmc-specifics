package me.earth.headlessmc.mc.player;

import me.earth.headlessmc.mc.Adapter;

/**
 * Represents the Minecraft player entity.
 */
public interface Player extends Adapter {
    /**
     * Sends a chat message.
     * If the message starts with a "/" a command will be send instead.
     *
     * @param message the message to send.
     */
    void sendMessage(String message);

    /**
     * Opens the menu, similar to pressing ESC.
     */
    void openMenu();

    /**
     * Opens the Inventory Menu, similar to pressing E.
     */
    void openInventory();

    /**
     * Closes the current screen.
     */
    void closeScreen();

    /**
     * @return {@code true} if the player is dead and can respawn, e.g. while the death screen is displayed.
     */
    boolean isDead();

    /**
     * Asks the server to respawn the player, similar to clicking "Respawn" on the death screen.
     */
    void requestRespawn();

}
