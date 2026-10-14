package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.ConsoleException;
import io.github.headlesshq.headlessmc.console.Password;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A simple {@link Console} without JLine, used if JLine is disabled or fails to load.
 * Writes to {@link FileDescriptor#out} because Minecraft redirects {@link System#out} into its logger.
 */
public class StdConsole implements Console {
    private final BufferedReader in;
    private final PrintStream out;

    public StdConsole() {
        this(System.in, new FileOutputStream(FileDescriptor.out));
    }

    public StdConsole(InputStream in, OutputStream out) {
        this.in = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
        this.out = new PrintStream(out, true);
    }

    @Override
    public synchronized void write(String message) {
        out.println(message);
    }

    @Override
    public synchronized void write(String message, String nextLine) {
        out.print(message + nextLine);
        out.flush();
    }

    @Override
    public String read() {
        try {
            String line = in.readLine();
            if (line == null) {
                throw new ConsoleException("Input has been closed");
            }

            return line;
        } catch (IOException e) {
            throw new ConsoleException(e);
        }
    }

    @Override
    public Password readPassword() {
        return Password.of(read());
    }

}
