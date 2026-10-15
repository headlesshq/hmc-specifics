package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.Player;

public class CommandCommand extends PrefixedCommand {
    public CommandCommand(MinecraftContext ctx) {
        super(ctx, "/", "Sends a chat command.", "/", "command");
    }

    @Override
    protected void executeUnprefixed(String unprefixedLine) {
        mc.scheduleEx(() -> {
            Player player = mc.getPlayer();
            if (player == null) {
                ctx.log("You need to be ingame to send a command!");
            } else {
                player.sendMessage("/" + unprefixedLine);
            }
        });
    }

}
