package me.earth.headlessmc.mc.commands.external;

import me.earth.headlessmc.mc.commands.MinecraftContext;
import picocli.CommandLine;

/**
 * A command like another mod would register it, which needs a {@link GreetCommandFactory}.
 */
@CommandLine.Command(name = "greet", description = "Greets someone.")
public class GreetCommand implements Runnable {
    @CommandLine.Parameters(index = "0")
    private String name;

    private final MinecraftContext ctx;
    private final String greeting;

    public GreetCommand(MinecraftContext ctx, String greeting) {
        this.ctx = ctx;
        this.greeting = greeting;
    }

    @Override
    public void run() {
        ctx.log(greeting + " " + name);
    }

}
