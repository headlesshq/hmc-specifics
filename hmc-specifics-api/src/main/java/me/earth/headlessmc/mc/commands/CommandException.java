package me.earth.headlessmc.mc.commands;

/**
 * Thrown by commands if something goes wrong.
 * Only the message will be displayed to the user, without a stacktrace.
 */
public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
