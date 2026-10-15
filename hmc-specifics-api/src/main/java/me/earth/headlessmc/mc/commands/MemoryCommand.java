package me.earth.headlessmc.mc.commands;

import picocli.CommandLine;

import java.text.DecimalFormat;

@CommandLine.Command(name = "memory", description = "Displays memory usage.")
public class MemoryCommand extends AbstractMinecraftCommand {
    public MemoryCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() {
        long total = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
        long free = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
        long max = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
        long used = total - free;
        String percent = new DecimalFormat("0.00").format(used * 100L / (double) max);
        ctx.log("-Used:  " + used + "mb, (" + percent + "%)");
        ctx.log("-Free:  " + free + "mb");
        ctx.log("-Total: " + total + "mb");
        ctx.log("-Max:   " + max + "mb");
    }

}
