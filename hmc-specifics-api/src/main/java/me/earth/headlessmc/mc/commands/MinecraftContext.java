package me.earth.headlessmc.mc.commands;

import io.github.headlesshq.headlessmc.auth.AuthService;
import io.github.headlesshq.headlessmc.auth.LastUsedAccountService;
import io.github.headlesshq.headlessmc.commands.auth.AccountCommand;
import io.github.headlesshq.headlessmc.console.ArgSplitter;
import io.github.headlesshq.headlessmc.console.Completions;
import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.completions.PicocliCompletions;
import io.github.headlesshq.headlessmc.console.format.SimpleTable;
import io.github.headlesshq.headlessmc.console.format.TableBuilder;
import io.github.headlesshq.headlessmc.console.format.TableProvider;
import io.github.headlesshq.headlessmc.exceptions.HeadlessMcException;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.auth.AccountRefreshingService;
import me.earth.headlessmc.mc.console.ConsoleWriter;
import me.earth.headlessmc.mc.console.QuotingArgSplitter;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes commands on a running Minecraft instance.
 * A new picocli {@link CommandLine} is created for every command, because picocli stores the parsed arguments
 * inside the command objects and {@link ScheduledCommand}s are executed later on the main thread.
 */
public class MinecraftContext {
    private static final Logger LOGGER = Logger.getLogger(MinecraftContext.class.getName());

    private final List<PrefixedCommand> prefixedCommands = new ArrayList<>();
    private final ArgSplitter argSplitter = new QuotingArgSplitter();
    private final AccountRefreshingService accountRefreshingService;
    private final LastUsedAccountService lastUsedAccountService;
    private final AuthService authService;
    private final Console console;
    private final Minecraft mc;

    public MinecraftContext(Minecraft mc,
                            Console console,
                            AuthService authService,
                            LastUsedAccountService lastUsedAccountService,
                            AccountRefreshingService accountRefreshingService) {
        this.mc = mc;
        this.console = console;
        this.authService = authService;
        this.lastUsedAccountService = lastUsedAccountService;
        this.accountRefreshingService = accountRefreshingService;
        prefixedCommands.add(new CommandCommand(this));
        prefixedCommands.add(new MessageCommand(this, ".", "."));
        prefixedCommands.add(new MessageCommand(this, "msg", "msg "));
    }

    public CommandLine createCommandLine() {
        CommandFactory factory = new CommandFactory(authService);
        CommandLine commandLine = new CommandLine(new RootCommand(), factory);
        commandLine.addSubcommand(new CommandLine.HelpCommand());
        commandLine.addSubcommand(new QuitCommand(this));
        commandLine.addSubcommand(new RenderCommand(this));
        commandLine.addSubcommand(new ClickCommand(this));
        commandLine.addSubcommand(new GuiCommand(this));
        commandLine.addSubcommand(new TextCommand(this));
        commandLine.addSubcommand(new CloseCommand(this));
        commandLine.addSubcommand(new MenuCommand(this));
        commandLine.addSubcommand(new MemoryCommand(this));
        commandLine.addSubcommand(new ConnectCommand(this));
        commandLine.addSubcommand(new DisconnectCommand(this));
        commandLine.addSubcommand(new KeyCommand(this));

        AccountCommand accountCommand = new AccountCommand(
            lastUsedAccountService, new SimpleTableProvider(), authService, console
        );

        CommandLine account = new CommandLine(accountCommand, factory);
        account.addSubcommand(new CurrentAccountCommand(this));
        account.addSubcommand(new TokenAccountCommand(this));
        account.addSubcommand(new AutoRefreshCommand(this));
        commandLine.addSubcommand(account);

        commandLine.setSubcommandsCaseInsensitive(true);
        PrintWriter writer = new PrintWriter(new ConsoleWriter(console), true);
        commandLine.setOut(writer);
        commandLine.setErr(writer);
        return commandLine;
    }

    public void execute(String line) {
        if (line.trim().isEmpty()) {
            return;
        }

        for (PrefixedCommand prefixedCommand : prefixedCommands) {
            if (prefixedCommand.matches(line)) {
                try {
                    prefixedCommand.execute(line);
                } catch (Throwable t) {
                    handleException(t);
                }

                return;
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

            return;
        } catch (CommandLine.ParameterException e) {
            log(e.getMessage());
            log("Use 'help " + e.getCommandLine().getCommandName() + "' for more information.");
            return;
        }

        if (CommandLine.printHelpIfRequested(parseResult)) {
            return;
        }

        List<CommandLine> commandLines = parseResult.asCommandLineList();
        Object command = commandLines.get(commandLines.size() - 1).getCommand();
        if (command instanceof ScheduledCommand) {
            mc.schedule(() -> execute(parseResult));
        } else {
            execute(parseResult);
        }
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

    public List<PrefixedCommand> getPrefixedCommands() {
        return Collections.unmodifiableList(prefixedCommands);
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

    private static final class SimpleTableProvider implements TableProvider {
        @Override
        public <T> TableBuilder<T> get() {
            return new SimpleTable<>();
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
