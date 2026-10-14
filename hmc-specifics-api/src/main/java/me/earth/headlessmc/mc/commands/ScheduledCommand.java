package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.Minecraft;

/**
 * Commands implementing this interface will be executed on Minecrafts
 * main thread via {@link Minecraft#schedule(Runnable)} by the {@link MinecraftContext}.
 * Exceptions thrown by these commands are caught and displayed by the {@link MinecraftContext}.
 */
public interface ScheduledCommand {
}
