package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;

import java.io.Writer;

/**
 * A {@link Writer} that writes to a {@link Console} when flushed.
 * Used for picocli's output.
 */
public class ConsoleWriter extends Writer {
    private final StringBuilder buffer = new StringBuilder();
    private final Console console;

    public ConsoleWriter(Console console) {
        this.console = console;
    }

    @Override
    public synchronized void write(char[] cbuf, int off, int len) {
        buffer.append(cbuf, off, len);
    }

    @Override
    public synchronized void flush() {
        if (buffer.length() == 0) {
            return;
        }

        String text = buffer.toString();
        buffer.setLength(0);
        if (text.endsWith(System.lineSeparator())) {
            text = text.substring(0, text.length() - System.lineSeparator().length());
        } else if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        console.write(text);
    }

    @Override
    public void close() {
        flush();
    }

}
