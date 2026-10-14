package me.earth.headlessmc.mc.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "disconnect", description = "Disconnects you from a server.")
public class DisconnectCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    public DisconnectCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        ctx.log("Disconnecting...");
        mc.disconnect();
    }

}
