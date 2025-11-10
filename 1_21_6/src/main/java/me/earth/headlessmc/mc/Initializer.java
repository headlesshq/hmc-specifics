package me.earth.headlessmc.mc;

import me.earth.headlessmc.api.config.ConfigImpl;
import me.earth.headlessmc.mc.mixins.IBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class Initializer {
    private static final Logger LOGGER = LogManager.getLogger(Initializer.class);

    public static void init(Minecraft mc) {
        LOGGER.info("Loading HMC-Specifics!");
        if (System.out.getClass().getName().startsWith(PrintStream.class.getName())) {
            LOGGER.info("System Streams have not been replaced, wrapping Streams");
            IBootstrap.invokeWrapStreams();
        }

        SpecificsInitializer initializer = new SpecificsInitializer(mc);
        initializer.init(ConfigImpl.empty());
    }

}
