package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.gui.GuiButton;
import me.earth.headlessmc.mc.gui.GuiElement;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.gui.TextField;
import me.earth.headlessmc.mc.util.ExtendedTable;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CommandLine.Command(name = "gui", description = "Dumps the currently displayed screen.")
public class GuiCommand extends AbstractGuiCommand implements ScheduledCommand {
    @CommandLine.Option(names = "--tooltip", paramLabel = "<id>", description = "Displays the tooltip of the element with the given id.")
    private String tooltip;

    @CommandLine.Option(names = "-v", description = "Displays the full class names of elements.")
    private boolean verbose;

    public GuiCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute(GuiScreen gui) throws CommandException {
        if (tooltip != null) {
            List<String> tooltipLines = gui.getAllElements()
                    .stream()
                    .filter(e -> tooltip.equals(String.valueOf(e.getId())))
                    .map(GuiElement::getTooltip)
                    .findFirst()
                    .orElse(null);

            if (tooltipLines == null || tooltipLines.isEmpty()) {
                ctx.log("No tooltip found.");
            } else {
                tooltipLines.forEach(ctx::log);
            }

            return;
        }

        List<GuiButton> guiButtons = gui.getButtons();
        guiButtons.sort(Comparator.comparingInt(GuiElement::getId));

        List<TextField> textFields = gui.getTextFields();
        textFields.sort(Comparator.comparingInt(GuiElement::getId));

        List<GuiElement> all = new ArrayList<>(gui.getAllElements());
        all.removeAll(guiButtons);
        all.removeAll(textFields);
        all.sort(Comparator.comparingInt(GuiElement::getId));

        ctx.log(String.format(
            "Screen: %s\nButtons:\n%s\nTextFields:\n%s",
            gui.getHandle().getClass().getName(),
            table(guiButtons).insert("on", "type", g -> g.isEnabled() ? "1" : "0").build(),
            table(textFields).build()));

        if (!all.isEmpty()) {
            ctx.log("Other:\n" + table(all).buildCalculatingAnsiWidth());
        }
    }

    private <T extends GuiElement> ExtendedTable<T> table(Iterable<T> elements) {
        return new ExtendedTable<T>()
            .addAll(elements)
            .withInt("id", GuiElement::getId)
            .withColumn("text", GuiElement::getText)
            .withInt("x", GuiElement::getX)
            .withInt("y", GuiElement::getY)
            .withInt("w", GuiElement::getWidth)
            .withInt("h", GuiElement::getHeight)
            .withColumn("type", g -> verbose
                ? g.getHandle().getClass().getName()
                : g.getHandle().getClass().getSimpleName());
    }

}
