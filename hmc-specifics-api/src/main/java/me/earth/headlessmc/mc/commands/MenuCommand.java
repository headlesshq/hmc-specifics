package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.player.Player;
import picocli.CommandLine;

@CommandLine.Command(name = "menu", description = "Opens the menu if you are ingame.")
public class MenuCommand extends AbstractPlayerCommand implements ScheduledCommand {
    @CommandLine.Option(names = "-inventory", description = "Opens the inventory instead.")
    private boolean inventory;

    public MenuCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute(Player player) {
        if (inventory) {
            ctx.log("Opening inventory...");
            player.openInventory();
        } else {
            ctx.log("Opening menu...");
            player.openMenu();
        }
    }

}
