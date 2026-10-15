package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.gui.TextField;
import picocli.CommandLine;

@CommandLine.Command(name = "text", description = "Set text in text fields.")
public class TextCommand extends AbstractGuiCommand implements ScheduledCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<id>", description = "The id of the text field.")
    private String id;

    @CommandLine.Parameters(index = "1", arity = "0..1", paramLabel = "<text>", description = "The text to set, use quotes for spaces.")
    private String text = "";

    public TextCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute(GuiScreen gui) throws CommandException {
        for (TextField textField : gui.getTextFields()) {
            if (id.equals(String.valueOf(textField.getId()))) {
                ctx.log(String.format("Setting text field %s to \"%s\".", id, text));
                textField.setText(text);
                return;
            }
        }

        throw new CommandException(String.format("Couldn't find text field with id %s", id));
    }

}
