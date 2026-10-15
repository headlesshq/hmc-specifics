package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.Minecraft;
import picocli.CommandLine;

/**
 * Base class for picocli commands registered on a {@link MinecraftContext}.
 */
public abstract class AbstractMinecraftCommand implements Runnable {
    protected final MinecraftContext ctx;
    protected final Minecraft mc;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message.")
    private boolean help;

    public AbstractMinecraftCommand(MinecraftContext ctx) {
        this.ctx = ctx;
        this.mc = ctx.getMc();
    }

    @Override
    public void run() {
        execute();
    }

    protected abstract void execute() throws CommandException;

}
