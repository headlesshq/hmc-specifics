package me.earth.headlessmc.mc.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "quit", aliases = {"exit", "stop"}, description = "Quits the running game.")
public class QuitCommand extends AbstractMinecraftCommand {
    @CommandLine.Option(names = "-force", description = "Calls System.exit instead of quitting Minecraft.")
    private boolean force;

    @CommandLine.Option(names = "-s", description = "Quits on the current thread instead of scheduling it on the main thread.")
    private boolean sync;

    public QuitCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() {
        if (force) {
            ctx.log("Forcing Minecraft to Exit!");
            System.exit(0);
            return;
        }

        if (sync) {
            ctx.log("Quitting Minecraft...");
            mc.quit();
        } else {
            mc.scheduleEx(() -> {
                ctx.log("Quitting Minecraft...");
                mc.quit();
            });
        }
    }

}
