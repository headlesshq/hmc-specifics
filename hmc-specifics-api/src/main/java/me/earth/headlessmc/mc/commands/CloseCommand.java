package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.Player;
import picocli.CommandLine;

@CommandLine.Command(name = "close", description = "Closes the current screen, if ingame.")
public class CloseCommand extends AbstractPlayerCommand implements ScheduledCommand {
    public CloseCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute(Player player) {
        ctx.log("Closing current screen...");
        player.closeScreen();
    }

}
