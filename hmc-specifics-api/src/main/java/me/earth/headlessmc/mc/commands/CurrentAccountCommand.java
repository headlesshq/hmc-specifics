package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.auth.McAccount;
import picocli.CommandLine;

@CommandLine.Command(name = "current", description = "Displays the account Minecraft is currently using.")
public class CurrentAccountCommand extends AbstractMinecraftCommand {
    public CurrentAccountCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        McAccount mcAccount = mc.getMcAccount();
        if (mcAccount == null) {
            throw new CommandException("Failed to find account!");
        }

        ctx.log("Current account: " + mcAccount.getName());
    }

}
