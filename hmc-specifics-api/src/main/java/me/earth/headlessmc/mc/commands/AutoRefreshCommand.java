package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.auth.AccountRefreshingService;
import picocli.CommandLine;

@CommandLine.Command(name = "autorefresh", description = "Manages the service that refreshes the current account automatically.")
public class AutoRefreshCommand extends AbstractMinecraftCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<action>", description = "One of: ${COMPLETION-CANDIDATES}")
    private Action action;

    public AutoRefreshCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        AccountRefreshingService service = ctx.getAccountRefreshingService();
        switch (action) {
            case start:
                ctx.log(service.start() ? "Account refresh started." : "Account refresh already running.");
                break;
            case stop:
                ctx.log(service.stop() ? "Account refresh stopped." : "Account refresh not running.");
                break;
            case status:
                ctx.log(service.isRunning() ? "Account refresh running." : "Account refresh not running.");
                break;
            case now:
                ctx.log(service.refresh() ? "Account refreshed successfully." : "Failed to refresh account.");
                break;
            default:
                throw new CommandException("Unknown action " + action);
        }
    }

    @SuppressWarnings("unused")
    public enum Action {
        start,
        stop,
        status,
        now
    }

}
