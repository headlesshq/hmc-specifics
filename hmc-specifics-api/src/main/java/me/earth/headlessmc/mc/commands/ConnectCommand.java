package me.earth.headlessmc.mc.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "connect", description = "Connects you to a server.")
public class ConnectCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<ip>", description = "The ip of the server.")
    private String ip;

    @CommandLine.Parameters(index = "1", arity = "0..1", paramLabel = "<port>", description = "The port of the server, 25565 by default.")
    private int port = 25565;

    public ConnectCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        ctx.log("Connecting to server " + ip + " at port " + port + "...");
        mc.connect(ip, port);
    }

}
