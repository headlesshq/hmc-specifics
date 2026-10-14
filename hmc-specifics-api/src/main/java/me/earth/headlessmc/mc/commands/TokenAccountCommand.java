package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.auth.McAccount;
import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command(name = "token", description = "Uses an account with the given name, uuid and access token, without verifying it.")
public class TokenAccountCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<name>", description = "The name of the account.")
    private String name;

    @CommandLine.Parameters(index = "1", paramLabel = "<uuid>", description = "The uuid of the account, or 'random'.")
    private String uuid;

    @CommandLine.Parameters(index = "2", paramLabel = "<token>", description = "The access token of the account.")
    private String token;

    public TokenAccountCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        UUID id;
        if ("random".equalsIgnoreCase(uuid)) {
            id = UUID.randomUUID();
        } else {
            try {
                id = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                throw new CommandException("Failed to parse \"" + uuid + "\" as a UUID!");
            }
        }

        ctx.log("Logging in with account " + name);
        mc.setMcAccount(new McAccount(name, id, token));
    }

}
