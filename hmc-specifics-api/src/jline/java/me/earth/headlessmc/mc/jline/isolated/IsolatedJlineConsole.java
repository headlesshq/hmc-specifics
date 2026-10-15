package me.earth.headlessmc.mc.jline.isolated;

import io.github.headlesshq.headlessmc.console.Completions;
import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.ConsoleException;
import io.github.headlesshq.headlessmc.console.ConsoleExtensions;
import io.github.headlesshq.headlessmc.console.Password;
import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Console} backed by a single, persistent JLine {@link Terminal}.
 * This class is loaded on an isolated classloader together with JLine,
 * it may only reference JLine, the JDK and {@code io.github.headlesshq.headlessmc} classes.
 * It is compiled against JLine 3 and against JLine 4.
 */
@SuppressWarnings("unused") // instantiated reflectively by JlineLoader
public final class IsolatedJlineConsole implements Console, ConsoleExtensions, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(IsolatedJlineConsole.class.getName());

    private final Terminal terminal;
    private final LineReader reader;
    private volatile Completions completions;

    public IsolatedJlineConsole(boolean dumb) throws IOException {
        this.terminal = buildTerminal(dumb);
        this.reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .appName("HeadlessMc")
            .parser(new DefaultParser())
            .completer(this::complete)
            .option(LineReader.Option.AUTO_LIST, true)
            .option(LineReader.Option.LIST_PACKED, true)
            .option(LineReader.Option.AUTO_MENU, true)
            .option(LineReader.Option.MENU_COMPLETE, true)
            .build();
    }

    @Override
    public void write(String message) {
        reader.printAbove(message);
    }

    @Override
    public void write(String message, String nextLine) {
        if (nextLine.contains("\n")) {
            reader.printAbove(message + nextLine);
        } else {
            terminal.writer().print(message + nextLine);
            terminal.writer().flush();
        }
    }

    @Override
    public String read() {
        return readLine(null, null, null);
    }

    @Override
    public String read(String message) {
        return readLine(message, null, null);
    }

    @Override
    public Password readPassword() {
        return Password.of(readLine(null, '*', null));
    }

    @Override
    public Password readPassword(String message) {
        return Password.of(readLine(message, '*', null));
    }

    @Override
    public String read(String prompt, Completions completions) {
        return readLine(prompt, null, completions);
    }

    @Override
    public String edit(String initialString) {
        if (Terminal.TYPE_DUMB.equalsIgnoreCase(terminal.getType())
            || Terminal.TYPE_DUMB_COLOR.equalsIgnoreCase(terminal.getType())) {
            return readLine("[" + initialString + "]:", null, null);
        }

        return wrap(() -> reader.readLine(null, null, (MaskingCallback) null, initialString));
    }

    @Override
    public int getWidth() {
        return Math.max(55, terminal.getWidth());
    }

    @Override
    public Optional<ConsoleExtensions> extensions() {
        return Optional.of(this);
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }

    private synchronized String readLine(String prompt, Character mask, Completions completions) {
        this.completions = completions;
        try {
            return wrap(() -> reader.readLine(prompt, mask));
        } finally {
            this.completions = null;
        }
    }

    private String wrap(ReadAction action) {
        try {
            return action.read();
        } catch (UserInterruptException e) {
            throw new ConsoleException.Interrupted(e);
        } catch (EndOfFileException e) {
            throw new ConsoleException("Terminal has been closed", e);
        }
    }

    private void complete(LineReader reader, ParsedLine parsedLine, List<Candidate> candidates) {
        Completions completions = this.completions;
        if (completions == null) {
            return;
        }

        try {
            for (Completions.Candidate candidate : completions.candidates(null, new ParsedLineAdapter(parsedLine))) {
                candidates.add(new Candidate(
                    AttributedString.stripAnsi(candidate.getName()),
                    candidate.getName(),
                    null,
                    candidate.getDescription(),
                    null,
                    null,
                    true
                ));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to complete " + parsedLine.line(), e);
        }
    }

    private static Terminal buildTerminal(boolean dumb) throws IOException {
        // JLine 3 looks up its TerminalProviders on the context classloader, which does not know about our JLine
        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(IsolatedJlineConsole.class.getClassLoader());
        try {
            return buildTerminalWithContextClassLoader(dumb);
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }

    private static Terminal buildTerminalWithContextClassLoader(boolean dumb) throws IOException {
        if (!dumb) {
            try {
                return TerminalBuilder.builder()
                    .name("HeadlessMc")
                    .system(true)
                    .build();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Failed to build system terminal, falling back to dumb terminal", t);
            }
        }

        // Minecraft replaces System.in/out, so we use the FileDescriptors directly
        return TerminalBuilder.builder()
            .name("HeadlessMc")
            .system(false)
            .streams(new FileInputStream(FileDescriptor.in), new FileOutputStream(FileDescriptor.out))
            .type(Terminal.TYPE_DUMB) // otherwise JLine 4 probes the terminal capabilities of TERM
            .dumb(true)
            .build();
    }

    @FunctionalInterface
    private interface ReadAction {
        String read();
    }

    private static final class ParsedLineAdapter implements Completions.Line {
        private final ParsedLine line;

        private ParsedLineAdapter(ParsedLine line) {
            this.line = line;
        }

        @Override
        public String word() {
            return line.word();
        }

        @Override
        public int wordCursor() {
            return line.wordCursor();
        }

        @Override
        public int wordIndex() {
            return line.wordIndex();
        }

        @Override
        public List<String> words() {
            return line.words();
        }

        @Override
        public String line() {
            return line.line();
        }

        @Override
        public int cursor() {
            return line.cursor();
        }
    }

}
