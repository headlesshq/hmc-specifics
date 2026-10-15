package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.gui.GuiScreen;

public abstract class AbstractGuiCommand extends AbstractMinecraftCommand {
    public AbstractGuiCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        GuiScreen gui = mc.getScreen();
        if (gui == null) {
            ctx.log("Minecraft is currently not displaying a Gui.");
        } else {
            execute(gui);
        }
    }

    protected abstract void execute(GuiScreen gui) throws CommandException;

}
