package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.gui.GuiButton;
import me.earth.headlessmc.mc.gui.GuiElement;
import me.earth.headlessmc.mc.gui.GuiScreen;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
    name = "click",
    description = "Performs clicks in the Gui.",
    customSynopsis = "click <id> [<button>] [-enable]%n   or: click p <x> <y> [<button>]"
)
public class ClickCommand extends AbstractGuiCommand implements ScheduledCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<id|p>", description = "The id of the element to click, or p to click at a position.")
    private String target;

    @CommandLine.Parameters(index = "1..*", arity = "0..3", paramLabel = "<value>", description = "<button>, or <x> <y> [<button>] when clicking at a position.")
    private List<Integer> values = new ArrayList<>();

    @CommandLine.Option(names = "-enable", description = "Enables the button before clicking it.")
    private boolean enable;

    public ClickCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    public void execute(GuiScreen gui) throws CommandException {
        int b = 0;
        int x;
        int y;

        if ("p".equalsIgnoreCase(target)) {
            if (values.size() < 2) {
                throw new CommandException("Expected x and y at positions 2 and 3!");
            }

            x = values.get(0);
            y = values.get(1);
            if (values.size() > 2) {
                b = values.get(2);
            }
        } else {
            GuiElement element = gui
                .getAllElements()
                .stream()
                .filter(e -> String.valueOf(e.getId()).equals(target))
                .findFirst()
                .orElse(null);

            if (element == null) {
                throw new CommandException(String.format("Couldn't find GuiElement with id %s", target));
            }

            if (element instanceof GuiButton && enable) {
                ((GuiButton) element).setEnabled(true);
            }

            x = element.getX() + element.getWidth() / 2;
            y = element.getY() + element.getHeight() / 2;
            if (!values.isEmpty()) {
                b = values.get(0);
            }
        }

        ctx.log(String.format("Clicking at x=%d, y=%d, button=%d", x, y, b));
        gui.click(x, y, b);
    }

}
