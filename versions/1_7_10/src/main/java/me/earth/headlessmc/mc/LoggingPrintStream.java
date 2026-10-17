package me.earth.headlessmc.mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This is just the 1.12.2 net.minecraft.util.LoggingPrintStream
 */
public class LoggingPrintStream extends PrintStream {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String domain;

    public LoggingPrintStream(String string, OutputStream outputStream) {
        super(outputStream);
        this.domain = string;
    }

    public void println(String string) {
        this.logString(string);
    }

    public void println(Object object) {
        this.logString(String.valueOf(object));
    }

    private void logString(String string) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[Math.min(3, stackTraceElements.length)];
        LOGGER.info("[{}]@.({}:{}): {}", this.domain, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), string);
    }
}
