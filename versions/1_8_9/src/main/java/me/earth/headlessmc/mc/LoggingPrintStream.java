package me.earth.headlessmc.mc;

import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingPrintStream extends PrintStream {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final String domain;

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

    protected void logString(String string) {
        LOGGER.info("[{}]: {}", this.domain, string);
    }

}
