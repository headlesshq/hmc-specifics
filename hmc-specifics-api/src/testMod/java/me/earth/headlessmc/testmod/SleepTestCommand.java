package me.earth.headlessmc.testmod;

import me.earth.headlessmc.mc.commands.MinecraftContext;
import picocli.CommandLine;

/**
 * Not scheduled, so it sleeps on the command thread without blocking Minecraft's main thread.
 * hmc-specifics-test.json uses it as a delay, because the WAIT step of HeadlessMc tests always times out.
 */
@CommandLine.Command(name = "sleep", description = "Sleeps on the command thread, then logs a message.")
public class SleepTestCommand implements Runnable {
    @CommandLine.Parameters(index = "0", paramLabel = "<seconds>", description = "How long to sleep.")
    private int seconds;

    private final MinecraftContext ctx;

    public SleepTestCommand(MinecraftContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ctx.log(TestMod.PREFIX + "Slept " + seconds + "s.");
    }

}
