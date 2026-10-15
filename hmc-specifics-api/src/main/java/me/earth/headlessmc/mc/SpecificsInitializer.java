package me.earth.headlessmc.mc;

import io.github.headlesshq.headlessmc.console.ArgSplitter;
import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.ConsoleException;
import io.github.headlesshq.headlessmc.console.ConsoleExtensions;
import me.earth.headlessmc.mc.auth.AccountRefreshingService;
import me.earth.headlessmc.mc.auth.HmcAuth;
import me.earth.headlessmc.mc.auth.McLastUsedAccountService;
import me.earth.headlessmc.mc.commands.MinecraftContext;
import me.earth.headlessmc.mc.console.JlineLoader;
import me.earth.headlessmc.mc.console.StdConsole;
import me.earth.headlessmc.mc.keyboard.HmcKeyboard;
import me.earth.headlessmc.mc.log4j.HMCLog4JAppender;
import me.earth.headlessmc.mc.metrics.DefaultMetrics;
import me.earth.headlessmc.mc.metrics.MetricsService;
import me.earth.headlessmc.mc.metrics.MetricsTracker;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.earth.headlessmc.mc.SpecificProperties.*;

public class SpecificsInitializer {
    private static final Logger LOGGER = Logger.getLogger(SpecificsInitializer.class.getName());
    private static final String PROMPT = "> ";

    private final JlineLoader jlineLoader = new JlineLoader();
    private final Minecraft mc;

    public SpecificsInitializer(Minecraft mc) {
        this.mc = mc;
        HmcKeyboard.getInstance().getMcKeyboard(mc);
    }

    public MinecraftContext init() {
        if (getBoolean(DEBUG_JLINE, false)) {
            Logger.getLogger("org.jline").setLevel(Level.ALL);
            Arrays.stream(Logger.getLogger("org.jline").getHandlers()).forEach(handler -> handler.setLevel(Level.ALL));
        }

        MetricsService metrics = MetricsService.getInstance();
        DefaultMetrics.registerAll(metrics, mc, MetricsTracker.getInstance());
        metrics.loadProviders(mc, SpecificsInitializer.class.getClassLoader());

        HMCLog4JAppender.closeTerminalConsoleAppender();
        Console console = createConsole();
        ArgSplitter argSplitter = createArgSplitter();

        HmcAuth auth = new HmcAuth(HmcAuth.getAuthDir());
        AccountRefreshingService accountRefreshingService = new AccountRefreshingService(auth.getAuthService(), mc);
        McLastUsedAccountService lastUsedAccountService = new McLastUsedAccountService(
            auth.getLastUsedAccountService(), accountRefreshingService, mc
        );

        MinecraftContext context = new MinecraftContext(
            mc, console, argSplitter, auth.getAuthService(), lastUsedAccountService, accountRefreshingService
        );

        MinecraftContext.setInstance(context);
        if (getBoolean(ACCOUNT_REFRESH_ENABLED, true)) {
            accountRefreshingService.start();
        }

        Thread thread = new Thread(() -> read(context), "HeadlessMc-CommandLine");
        thread.setDaemon(true);
        thread.start();
        return context;
    }

    private Console createConsole() {
        if (getBoolean(JLINE_ENABLED, true) && jlineLoader.isBundlePresent()) {
            try {
                Console console = jlineLoader.loadConsole(getBoolean(JLINE_DUMB, false) || isRunningInCI());
                HMCLog4JAppender.install(console);
                return console;
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Failed to load JLine, falling back to simple console", t);
            }
        }

        return new StdConsole();
    }

    /**
     * The JLine parser does not need a terminal, so we also use it if the JLine console is disabled.
     */
    private ArgSplitter createArgSplitter() {
        if (jlineLoader.isBundlePresent()) {
            try {
                return jlineLoader.loadArgSplitter();
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Failed to load JLine ArgSplitter, falling back to splitting at whitespace", t);
            }
        }

        return line -> line.trim().split("\\s+");
    }

    private void read(MinecraftContext context) {
        Console console = context.getConsole();
        Optional<ConsoleExtensions> extensions = console.extensions();
        while (!Thread.currentThread().isInterrupted()) {
            String line;
            try {
                line = extensions.isPresent()
                    ? extensions.get().read(PROMPT, context.getCompletions())
                    : console.read();
            } catch (ConsoleException.Interrupted e) {
                continue;
            } catch (ConsoleException e) {
                LOGGER.log(Level.WARNING, "Stopped reading commands", e);
                return;
            }

            try {
                context.execute(line);
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Failed to execute " + line, t);
            }
        }
    }

    /**
     * E.g. Github action runners do not provide an interactive Terminal, so we use a dumb one.
     */
    private static boolean isRunningInCI() {
        try {
            Class.forName("me.earth.mc_runtime_test.McRuntimeTest");
            LOGGER.warning("Probably running inside CI, using dumb Terminal");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
