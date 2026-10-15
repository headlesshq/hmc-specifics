package me.earth.headlessmc.testmod;

import me.earth.headlessmc.mc.commands.MinecraftContext;
import me.earth.headlessmc.mc.commands.ScheduledCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "scheduled", description = "Runs on Minecraft's main thread.")
public class ScheduledTestCommand implements Runnable, ScheduledCommand {
    private final MinecraftContext ctx;

    public ScheduledTestCommand(MinecraftContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.log(TestMod.PREFIX + "Scheduled command executed.");
    }

}
