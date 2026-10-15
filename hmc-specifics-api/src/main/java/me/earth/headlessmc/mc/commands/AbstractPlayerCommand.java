package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.Player;

public abstract class AbstractPlayerCommand extends AbstractMinecraftCommand {
    public AbstractPlayerCommand(MinecraftContext ctx) {
        super(ctx);
    }

    protected abstract void execute(Player player) throws CommandException;

    @Override
    protected void execute() throws CommandException {
        Player player = mc.getPlayer();
        if (player == null) {
            throw new CommandException("You are not ingame!");
        }

        execute(player);
    }

}
