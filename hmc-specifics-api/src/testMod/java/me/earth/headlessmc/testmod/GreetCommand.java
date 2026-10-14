package me.earth.headlessmc.testmod;

import me.earth.headlessmc.mc.commands.MinecraftContext;
import picocli.CommandLine;

/**
 * Needs a {@link GreetCommandFactory}, because of its constructor.
 * Its subcommand is declared with picocli and instantiated through its {@link MinecraftContext} constructor.
 */
@CommandLine.Command(name = "hmcs-test", description = "Commands of the HMC-Specifics test mod.", subcommands = ScheduledTestCommand.class)
public class GreetCommand implements Runnable {
    @CommandLine.Parameters(index = "0", arity = "0..1", paramLabel = "<name>", description = "Who to greet.")
    private String name = "World";

    private final MinecraftContext ctx;
    private final String greeting;

    public GreetCommand(MinecraftContext ctx, String greeting) {
        this.ctx = ctx;
        this.greeting = greeting;
    }

    @Override
    public void run() {
        ctx.log(TestMod.PREFIX + greeting + " " + name + "!");
    }

}
