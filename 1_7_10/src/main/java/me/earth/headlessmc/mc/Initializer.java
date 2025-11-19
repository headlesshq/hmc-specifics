package me.earth.headlessmc.mc;

import me.earth.headlessmc.api.config.ConfigImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class Initializer {
    private static final Logger LOGGER = LogManager.getLogger(Initializer.class);

    public static void init(Minecraft mc) {
        LOGGER.info("Loading HMC-Specifics!");
        if (System.out.getClass().getName().startsWith(PrintStream.class.getName())) {
            LOGGER.info("System Streams have not been replaced, wrapping Streams");
            System.setErr(new LoggingPrintStream("STDERR", System.err));
            System.setOut(new LoggingPrintStream("STDOUT", System.out));
        }

        SpecificsInitializer initializer = new SpecificsInitializer(mc);
        initializer.init(ConfigImpl.empty());
        LOGGER.info("HMC-Specifics initialized!");
    }

}
