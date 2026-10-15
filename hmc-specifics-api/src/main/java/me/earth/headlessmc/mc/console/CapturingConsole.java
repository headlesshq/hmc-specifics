package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.ConsoleException;
import io.github.headlesshq.headlessmc.console.Password;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Console} that collects what commands write, e.g. to send the output of a command executed with
 * {@link me.earth.headlessmc.mc.commands.MinecraftContext#execute(String, Console)} somewhere else.
 * It can not be read from, commands that ask for input fail.
 */
public class CapturingConsole implements Console {
    private final List<String> lines = new ArrayList<>();
    private final Console delegate;

    public CapturingConsole() {
        this(null);
    }

    /**
     * @param delegate a console to also write everything to, e.g. the console of the game, or {@code null}.
     */
    public CapturingConsole(Console delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(String message) {
        synchronized (lines) {
            lines.add(message);
        }

        if (delegate != null) {
            delegate.write(message);
        }
    }

    @Override
    public void write(String message, String nextLine) {
        write(message + nextLine);
    }

    @Override
    public String read() {
        throw new ConsoleException("This console can not be read from");
    }

    @Override
    public Password readPassword() {
        throw new ConsoleException("This console can not be read from");
    }

    /**
     * @return the lines written so far.
     */
    public List<String> getLines() {
        synchronized (lines) {
            return new ArrayList<>(lines);
        }
    }

    /**
     * @return the lines written so far, separated by line breaks.
     */
    public String getOutput() {
        return String.join("\n", getLines());
    }

}
