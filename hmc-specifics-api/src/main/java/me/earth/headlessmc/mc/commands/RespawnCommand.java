package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.AutoRespawn;
import me.earth.headlessmc.mc.player.Player;
import picocli.CommandLine;

@CommandLine.Command(
    name = "respawn",
    description = "Respawns you if you are dead, or manages AutoRespawn.",
    customSynopsis = "respawn [now]%n" +
        "   or: respawn auto [on|off]%n" +
        "   or: respawn status"
)
public class RespawnCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    @CommandLine.Parameters(index = "0", arity = "0..1", paramLabel = "<action>", description = "One of: ${COMPLETION-CANDIDATES}. 'now' by default.")
    private Action action = Action.now;

    @CommandLine.Parameters(index = "1", arity = "0..1", paramLabel = "<state>", description = "For 'auto', one of: ${COMPLETION-CANDIDATES}. Toggles AutoRespawn if not specified.")
    private State state;

    public RespawnCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        if (state != null && action != Action.auto) {
            throw new CommandException("'" + state + "' can only be used with 'respawn auto'.");
        }

        AutoRespawn autoRespawn = AutoRespawn.getInstance();
        switch (action) {
            case now:
                Player player = mc.getPlayer();
                if (player == null) {
                    throw new CommandException("You are not ingame!");
                }

                if (!player.isDead()) {
                    throw new CommandException("You are not dead!");
                }

                ctx.log("Respawning...");
                player.requestRespawn();
                break;
            case auto:
                autoRespawn.setEnabled(state == null ? !autoRespawn.isEnabled() : state == State.on);
                logAutoRespawn(autoRespawn);
                break;
            case status:
                logAutoRespawn(autoRespawn);
                break;
            default:
                throw new CommandException("Unknown action " + action);
        }
    }

    private void logAutoRespawn(AutoRespawn autoRespawn) {
        ctx.log("AutoRespawn " + (autoRespawn.isEnabled() ? "enabled." : "disabled."));
    }

    @SuppressWarnings("unused")
    public enum Action {
        now,
        auto,
        status
    }

    @SuppressWarnings("unused")
    public enum State {
        on,
        off
    }

}
