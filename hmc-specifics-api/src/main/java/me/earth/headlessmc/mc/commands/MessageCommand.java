package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.Player;

public class MessageCommand extends PrefixedCommand {
    public MessageCommand(MinecraftContext ctx, String name, String prefix) {
        super(ctx, name, "Sends a chat message.", prefix, "message");
    }

    @Override
    protected void executeUnprefixed(String unprefixedLine) {
        mc.scheduleEx(() -> {
            Player player = mc.getPlayer();
            if (player == null) {
                ctx.log("You need to be ingame to send a message!");
            } else {
                player.sendMessage(unprefixedLine);
            }
        });
    }

}
