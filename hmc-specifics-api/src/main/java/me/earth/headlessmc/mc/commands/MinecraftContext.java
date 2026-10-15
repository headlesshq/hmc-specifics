package me.earth.headlessmc.mc.commands;

import io.github.headlesshq.headlessmc.auth.AuthService;
import io.github.headlesshq.headlessmc.auth.LastUsedAccountService;
import io.github.headlesshq.headlessmc.console.ArgSplitter;
import io.github.headlesshq.headlessmc.console.Completions;
import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.completions.PicocliCompletions;
import io.github.headlesshq.headlessmc.exceptions.HeadlessMcException;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.auth.AccountRefreshingService;
import me.earth.headlessmc.mc.console.ConsoleWriter;
import me.earth.headlessmc.mc.metrics.MetricsService;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes commands on a running Minecraft instance.
 * The picocli commands are collected by a {@link CommandRegistry}.
 * A new picocli {@link CommandLine} is created for every command, because picocli stores the parsed arguments
 * inside the command objects and {@link ScheduledCommand}s are executed later on the main thread.
 * Other mods can execute commands with the context HMC-Specifics reads commands with, see {@link #getInstance()},
 * and capture their output with {@link #execute(String, Console)}.
 */
public class MinecraftContext {
    private static final Logger LOGGER = Logger.getLogger(MinecraftContext.class.getName());
    private static volatile MinecraftContext instance;

    private final List<PrefixedCommand> prefixedCommands = new ArrayList<>();
    private final ClassLoader classLoader = MinecraftContext.class.getClassLoader();
    private volatile Commands commands;
    private final AccountRefreshingService accountRefreshingService;
    private final LastUsedAccountService lastUsedAccountService;
    private final AuthService authService;
    private final ArgSplitter argSplitter;
    private final Console console;
    private final Minecraft mc;

    public MinecraftContext(Minecraft mc,
                            Console console,
                            ArgSplitter argSplitter,
                            AuthService authService,
                            LastUsedAccountService lastUsedAccountService,
                            AccountRefreshingService accountRefreshingService) {
        this.mc = mc;
        this.console = console;
        this.argSplitter = argSplitter;
        this.authService = authService;
        this.lastUsedAccountService = lastUsedAccountService;
        this.accountRefreshingService = accountRefreshingService;
        prefixedCommands.add(new CommandCommand(this));
        prefixedCommands.add(new MessageCommand(this, ".", "."));
        prefixedCommands.add(new MessageCommand(this, "msg", "msg "));
        updateCommands();
    }

    /**
     * A copy of the given context that writes to another console and shares the loaded commands.
     */
    private MinecraftContext(MinecraftContext context, Console console) {
        this.mc = context.mc;
        this.console = console;
        this.argSplitter = context.argSplitter;
        this.authService = context.authService;
        this.lastUsedAccountService = context.lastUsedAccountService;
        this.accountRefreshingService = context.accountRefreshingService;
        this.commands = context.updateCommands();
        prefixedCommands.add(new CommandCommand(this));
        prefixedCommands.add(new MessageCommand(this, ".", "."));
        prefixedCommands.add(new MessageCommand(this, "msg", "msg "));
    }

    /**
     * @return the context HMC-Specifics reads commands from the console with, or {@code null} if it has not been initialized yet.
     */
    public static MinecraftContext getInstance() {
        return instance;
    }

    /**
     * Called by HMC-Specifics when it has been initialized.
     *
     * @param context the context HMC-Specifics reads commands from the console with.
     */
    public static void setInstance(MinecraftContext context) {
        instance = context;
    }

    /**
     * @param console the console the copy writes to.
     * @return a copy of this context, whose commands write their output to the given console.
     */
    public MinecraftContext withConsole(Console console) {
        return new MinecraftContext(this, console);
    }

    /**
     * Executes a command and writes its output to the given console, instead of the console of this context.
     * E.g. use a {@link me.earth.headlessmc.mc.console.CapturingConsole} to collect the output.
     *
     * @param line the command to execute.
     * @param console the console to write the output to.
     * @return a future that completes once the command has been executed, see {@link #executeAsync(String)}.
     */
    public CompletableFuture<Void> execute(String line, Console console) {
        return withConsole(console).executeAsync(line);
    }

    public CommandLine createCommandLine() {
        Commands commands = updateCommands();
        CommandLine commandLine = createRootCommandLine(commands.registry);
        for (CommandRegistry.Entry command : commands.entries) {
            addCommand(commandLine, command);
        }

        commandLine.setSubcommandsCaseInsensitive(true);
        PrintWriter writer = new PrintWriter(new ConsoleWriter(console), true);
        commandLine.setOut(writer);
        commandLine.setErr(writer);
        return commandLine;
    }

    public void execute(String line) {
        executeAsync(line);
    }

    /**
     * Executes a command.
     *
     * @param line the command to execute.
     * @return a future that completes once the command has been executed, for {@link ScheduledCommand}s that is
     * when Minecraft's main thread has executed them. Commands that start threads, e.g. render, might still log afterwards.
     * The future never completes exceptionally, errors are written to the console like for every command.
     */
    public CompletableFuture<Void> executeAsync(String line) {
        CompletableFuture<Void> done = CompletableFuture.completedFuture(null);
        if (line.trim().isEmpty()) {
            return done;
        }

        for (PrefixedCommand prefixedCommand : prefixedCommands) {
            if (prefixedCommand.matches(line)) {
                try {
                    prefixedCommand.execute(line);
                } catch (Throwable t) {
                    handleException(t);
                }

                return done;
            }
        }

        CommandLine commandLine = createCommandLine();
        CommandLine.ParseResult parseResult;
        try {
            parseResult = commandLine.parseArgs(argSplitter.split(line));
        } catch (CommandLine.UnmatchedArgumentException e) {
            if (e.getCommandLine() == commandLine) {
                log("Couldn't find command for '" + line + "'. Use 'help' to list all commands.");
                List<String> suggestions = e.getSuggestions();
                if (!suggestions.isEmpty()) {
                    log("Did you mean: " + String.join(", ", suggestions) + "?");
                }
            } else {
                log(e.getMessage());
            }

            return done;
        } catch (CommandLine.ParameterException e) {
            log(e.getMessage());
            log("Use 'help " + e.getCommandLine().getCommandName() + "' for more information.");
            return done;
        }

        if (CommandLine.printHelpIfRequested(parseResult)) {
            return done;
        }

        List<CommandLine> commandLines = parseResult.asCommandLineList();
        Object command = commandLines.get(commandLines.size() - 1).getCommand();
        if (command instanceof ScheduledCommand) {
            CompletableFuture<Void> scheduled = new CompletableFuture<>();
            mc.schedule(() -> {
                try {
                    execute(parseResult);
                } finally {
                    scheduled.complete(null);
                }
            });
            return scheduled;
        }

        execute(parseResult);
        return done;
    }

    public Completions getCompletions() {
        return new MinecraftCompletions(new PicocliCompletions(this::createCommandLine));
    }

    public void log(String message) {
        console.write(message);
    }

    public Minecraft getMc() {
        return mc;
    }

    public Console getConsole() {
        return console;
    }

    public AccountRefreshingService getAccountRefreshingService() {
        return accountRefreshingService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public LastUsedAccountService getLastUsedAccountService() {
        return lastUsedAccountService;
    }

    public MetricsService getMetricsService() {
        return MetricsService.getInstance();
    }

    public List<CommandRegistry.Entry> getCommands() {
        return updateCommands().entries;
    }

    public List<PrefixedCommand> getPrefixedCommands() {
        return Collections.unmodifiableList(prefixedCommands);
    }

    private CommandLine createRootCommandLine(CommandRegistry registry) {
        return new CommandLine(new RootCommand(), new PicocliFactory(this, registry.getFactories()));
    }

    /**
     * Reloads the {@link CommandRegistry} if mods have been added to the classpath since it was last loaded.
     */
    private synchronized Commands updateCommands() {
        Commands current = commands;
        if (current == null || !current.registry.isUpToDate(classLoader)) {
            CommandRegistry registry = CommandRegistry.load(classLoader);
            current = new Commands(registry, validateCommands(registry));
            commands = current;
        }

        return current;
    }

    /**
     * Adds every command once, so that commands which can not be instantiated
     * or whose names clash with other commands are only reported once, when they are loaded.
     */
    private List<CommandRegistry.Entry> validateCommands(CommandRegistry registry) {
        List<CommandRegistry.Entry> result = new ArrayList<>();
        CommandLine commandLine = createRootCommandLine(registry);
        for (CommandRegistry.Entry entry : registry.getCommands()) {
            try {
                addCommand(commandLine, entry);
                result.add(entry);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to register command " + entry, e);
            }
        }

        return result;
    }

    private void addCommand(CommandLine root, CommandRegistry.Entry entry) {
        CommandLine parent = root;
        for (String name : entry.getParents()) {
            parent = parent.getSubcommands().get(name);
            if (parent == null) {
                throw new IllegalArgumentException("Failed to find parent command '" + name + "' for " + entry);
            }
        }

        parent.addSubcommand(entry.getType());
    }

    private void execute(CommandLine.ParseResult parseResult) {
        try {
            new CommandLine.RunLast().execute(parseResult);
        } catch (CommandLine.ExecutionException e) {
            handleException(e.getCause() == null ? e : e.getCause());
        } catch (Throwable t) {
            handleException(t);
        }
    }

    private void handleException(Throwable throwable) {
        if (throwable instanceof CommandException) {
            log(throwable.getMessage());
        } else if (throwable instanceof IllegalArgumentException || throwable instanceof HeadlessMcException) {
            LOGGER.log(Level.FINE, "Command failed", throwable);
            log(throwable.getMessage());
        } else {
            LOGGER.log(Level.SEVERE, "Command failed", throwable);
            log("An error occurred: " + throwable);
        }
    }

    private static final class Commands {
        private final CommandRegistry registry;
        private final List<CommandRegistry.Entry> entries;

        private Commands(CommandRegistry registry, List<CommandRegistry.Entry> entries) {
            this.registry = registry;
            this.entries = entries;
        }
    }

    @CommandLine.Command(
        name = "",
        description = "HMC-Specifics, control Minecraft from the command line.",
        synopsisHeading = "",
        customSynopsis = "",
        commandListHeading = "Commands:%n",
        footerHeading = "%nChat:%n",
        footer = {
            "  /<command>        Sends a chat command.",
            "  . <message>       Sends a chat message.",
            "  msg <message>     Sends a chat message."
        }
    )
    private static final class RootCommand implements Runnable {
        @Override
        public void run() {
            // NOP
        }
    }

    /**
     * Completes chat commands starting with '/' through Minecraft, all other commands through picocli.
     */
    private final class MinecraftCompletions implements Completions {
        private final Completions picocli;

        private MinecraftCompletions(Completions picocli) {
            this.picocli = picocli;
        }

        @Override
        public Iterable<Candidate> candidates(Object spec, Line line) {
            String text = line.line();
            if (text.startsWith("/")) {
                List<Candidate> candidates = new ArrayList<>();
                boolean prefix = line.words().size() <= 1 && !text.endsWith(" ");
                for (Map.Entry<String, String> entry : mc.getCompletions(text)) {
                    String name = prefix ? "/" + entry.getKey() : entry.getKey();
                    candidates.add(new Candidate(name, entry.getValue()));
                }

                return candidates;
            }

            for (PrefixedCommand prefixedCommand : prefixedCommands) {
                if (prefixedCommand.matches(text) && line.wordIndex() > 0) {
                    return Collections.emptyList();
                }
            }

            return picocli.candidates(spec, line);
        }

        @Override
        public Iterator<String> iterator() {
            return picocli.iterator();
        }
    }

}
