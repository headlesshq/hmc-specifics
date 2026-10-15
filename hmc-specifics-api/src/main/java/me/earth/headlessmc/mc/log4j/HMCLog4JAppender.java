package me.earth.headlessmc.mc.log4j;

import io.github.headlesshq.headlessmc.console.Console;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes log messages to a HeadlessMC {@link Console}, which prints them above the JLine LineReader.
 * That we we get a persistent input line, even if the program puts out lots of text asynchronously.
 * @see <a href=https://github.com/Minecrell/TerminalConsoleAppender>https://github.com/Minecrell/TerminalConsoleAppender</a>
 */
// Core.CATEGORY_NAME and Appender.ELEMENT_TYPE are constants that do not exist on older versions of Log4J but its no problem because they are constants and get inlined
@Plugin(name = HMCLog4JAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class HMCLog4JAppender extends AbstractAppender {
    public static final boolean ANSI_ENABLED = Boolean.parseBoolean(System.getProperty("hmc.log4jappender.ansi", "true"));
    public static final String PLUGIN_NAME = "HMCLog4JAppender";
    private static final PrintStream SYS_ERR = new PrintStream(new FileOutputStream(FileDescriptor.err), true);

    private static final String ANSI_RESET = "\u001B[m";
    private static final String ANSI_ERROR = "\u001B[31;1m"; // Bold Red
    private static final String ANSI_WARN = "\u001B[33;1m"; // Bold Yellow

    private static HMCLog4JAppender instance;

    private static final Logger LOGGER = LogManager.getLogger(HMCLog4JAppender.class);
    private final Console console;

    @SuppressWarnings("deprecation") // we need to call the deprecated super-constructor, the non-deprecated one might not exist
    protected HMCLog4JAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Console console) {
        super(name, filter, layout, ignoreExceptions/*, Property.EMPTY_ARRAY*/);
        this.console = console;
        instance = this;
    }

    @Override
    public void append(LogEvent event) {
        synchronized (this) {
            String text = getLayout().toSerializable(event).toString();
            if (ANSI_ENABLED) {
                Level level = event.getLevel();
                // if (level.isMoreSpecificThan(Level.ERROR)) { // not available on all log4j versions
                if (level.intLevel() <= Level.ERROR.intLevel()) {
                    text = ANSI_ERROR + text + ANSI_RESET;
                // } else if (level.isMoreSpecificThan(Level.WARN)) { // not available on all log4j versions
                } else if (level.intLevel() <= Level.WARN.intLevel()) {
                    text = ANSI_WARN + text + ANSI_RESET;
                }
            }

            if (text.endsWith(System.lineSeparator())) {
                console.write(text.substring(0, text.length() - System.lineSeparator().length()));
            } else if (text.endsWith("\n")) {
                console.write(text.substring(0, text.length() - 1));
            } else {
                console.write(text, "");
            }
        }
    }

    public Console getConsole() {
        return console;
    }

    public static HMCLog4JAppender getInstance() {
        return instance;
    }

    public static void install(Console console) {
        LOGGER.info(System.out);
        LOGGER.info(System.out.getClass().getClassLoader());
        LOGGER.info(HMCLog4JAppender.class.getClassLoader());
        LoggerContextFactory logFactory = LogManager.getFactory();
        if (logFactory instanceof Log4jContextFactory) {
            ((Log4jContextFactory) logFactory).getSelector().getLoggerContexts().forEach(context -> install(context, console));
        }
    }

    // this is kinda gross and I am not sure what I am doing but it works!
    @SuppressWarnings("RedundantSuppression") // RedundantArrayCreation
    private static void install(LoggerContext context, Console console) {
        // TODO: it seems that in 1.7.10 there are loggers that we have not installed ourselves on?
        Configuration config = context.getConfiguration();
        LoggerConfig rootLoggerConfig;
        try {
            rootLoggerConfig = config.getRootLogger();
        } catch (Throwable throwable) { // older versions of Log4J do not have this method
            LOGGER.debug("Failed to access config.getRootLogger", throwable);
            rootLoggerConfig = config.getLoggerConfig("");
        }

        Map<String, Appender> appenders = new HashMap<>(config.getAppenders());
        try {
            Layout<?> layout = null;
            for (Map.Entry<String, Appender> entry : appenders.entrySet()) {
                String name = entry.getKey();
                Appender appender = entry.getValue();
                Layout<?> appenderLayout = appender.getLayout();
                // we have to specifically tell Log4J which method to use, or else this happens:
                // Caused by: java.lang.NoSuchMethodError: org.apache.logging.log4j.Logger.info(Ljava/lang/String;<4x Ljava/lang/Object;>)V
                //noinspection RedundantArrayCreation
                LOGGER.info("{}: {}, {}, {}", new Object[]{ name, appender, appender.getClass(),
                    appenderLayout instanceof PatternLayout ? ((PatternLayout) appenderLayout).getConversionPattern() : appenderLayout });
                if (appenderLayout instanceof PatternLayout && !(((PatternLayout) appenderLayout).getConversionPattern()).contains("nolookups")) {
                    for (int i = 0; i < 100; i++) {
                        LOGGER.fatal("PatternLayout does not contain {nolookups}!!!!!!!!!! You are potentially vulnerable to the Log4J exploit!");
                    }
                }

                // its possible that there is a TerminalConsoleAppender, should we hijack that instead of removing it
                if (appender instanceof ConsoleAppender || "Console".equals(appender.getName())) {
                    if ("TerminalConsoleAppender".equals(appender.getClass().getSimpleName())) {
                        LOGGER.info("There is a TerminalConsoleAppender installed!");
                        LOGGER.info(TerminalConsoleAppender.class.getClassLoader());
                        LOGGER.info(appender.getClass().getClassLoader());
                    }

                    //noinspection RedundantArrayCreation
                    LOGGER.info("Removing appender {}", new Object[]{ name });
                    rootLoggerConfig.removeAppender(name);
                    layout = appenderLayout;
                }
            }

            if (layout == null) {
                LOGGER.error("Failed to remove an Appender! Cannot install " + PLUGIN_NAME);
                return;
            }

            // layout = PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg{nolookups}%n").build();
            HMCLog4JAppender appender = new HMCLog4JAppender(PLUGIN_NAME, null, layout, true, console);
            appender.start();

            try {
                config.addAppender(appender);
            } catch (Throwable throwable) { // older versions of Log4J do not have this method
                LOGGER.debug("Failed to access config.addAppender", throwable);
            }

            rootLoggerConfig.addAppender(appender, Level.INFO, null);
            context.updateLoggers();
        } catch (Throwable throwable) {
            SYS_ERR.println("Failed to add " + PLUGIN_NAME);
            throwable.printStackTrace(SYS_ERR);
            try {
                Map<String, Appender> appendersNow = new HashMap<>(config.getAppenders());
                for (Map.Entry<String, Appender> entry : appenders.entrySet()) {
                    if (!appendersNow.containsKey(entry.getKey())) {
                        rootLoggerConfig.addAppender(entry.getValue(), Level.DEBUG, null);
                    }
                }

                context.updateLoggers();
            } catch (Throwable throwable2) {
                SYS_ERR.println("Failed to reinstall Log4JAppenders");
                throwable.printStackTrace(SYS_ERR);
            }
        }
    }

    // TODO: it would be even cooler if we could instead make use of the TerminalConsoleAppender terminal?
    public static void closeTerminalConsoleAppender() {
        try {
            LOGGER.info("Closing TerminalConsoleAppender");
            TerminalConsoleAppender.close();
        } catch (Throwable t) {
            LOGGER.debug("Failed to close TerminalConsoleAppender", t);
        }
    }

}
