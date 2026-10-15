package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.Minecraft;

import java.util.Locale;

/**
 * A command that is not parsed by picocli, but matched by a prefix, e.g. {@code /gamemode creative}.
 * The rest of the line is passed on as is, without splitting it into arguments.
 */
public abstract class PrefixedCommand {
    protected final MinecraftContext ctx;
    protected final Minecraft mc;
    private final String name;
    private final String description;
    private final String prefix;
    private final String message;

    public PrefixedCommand(MinecraftContext ctx, String name, String description, String prefix, String message) {
        this.ctx = ctx;
        this.mc = ctx.getMc();
        this.name = name;
        this.description = description;
        this.prefix = prefix;
        this.message = message;
    }

    protected abstract void executeUnprefixed(String unprefixedLine) throws CommandException;

    public void execute(String line) throws CommandException {
        String unprefixedLine = line;
        String lowerLine = line.toLowerCase(Locale.ENGLISH);
        if (lowerLine.startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
            unprefixedLine = line.substring(prefix.length());
        } else if (lowerLine.startsWith(name.toLowerCase(Locale.ENGLISH))) {
            unprefixedLine = line.substring(name.length());
        }

        if (unprefixedLine.trim().isEmpty()) {
            throw new CommandException("Please specify a " + message);
        }

        executeUnprefixed(unprefixedLine);
    }

    public boolean matches(String line) {
        String lowerLine = line.toLowerCase(Locale.ENGLISH);
        return lowerLine.startsWith(prefix.toLowerCase(Locale.ENGLISH))
            || lowerLine.equals(name.toLowerCase(Locale.ENGLISH))
            || lowerLine.startsWith(name.toLowerCase(Locale.ENGLISH) + " ");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }

}
