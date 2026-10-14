package me.earth.headlessmc.mc.commands.external;

import me.earth.headlessmc.mc.commands.MinecraftContext;
import picocli.CommandLine;

/**
 * A subcommand of the account command, instantiated with its {@link MinecraftContext} constructor.
 */
@CommandLine.Command(name = "info", description = "Prints account info.")
public class AccountInfoCommand implements Runnable {
    private final MinecraftContext ctx;

    public AccountInfoCommand(MinecraftContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.log("Account info");
    }

}
