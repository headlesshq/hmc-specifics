package me.earth.headlessmc.mc;

import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.classloading.ApiClassloadingHelper;
import me.earth.headlessmc.api.config.Config;
import me.earth.headlessmc.jline.JLineProperties;
import me.earth.headlessmc.mc.commands.MinecraftContext;
import me.earth.headlessmc.mc.jline.VersionAgnosticJLineCommandLineReader;
import me.earth.headlessmc.mc.keyboard.HmcKeyboard;
import me.earth.headlessmc.mc.log4j.HMCLog4JAppender;
import me.earth.headlessmc.runtime.RuntimeInitializer;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.earth.headlessmc.mc.SpecificProperties.ACCOUNT_REFRESH_ENABLED;
import static me.earth.headlessmc.mc.SpecificProperties.DEBUG_JLINE;

public class SpecificsInitializer extends RuntimeInitializer {
    private final Minecraft mc;

    public SpecificsInitializer(Minecraft mc) {
        this.mc = mc;
        HmcKeyboard.getInstance().getMcKeyboard(mc);
    }

    public void init(Config config) {
        super.init(config);
    }

    @Override
    protected void createCommandContext(HeadlessMc hmc) {
        MinecraftContext context = new MinecraftContext(hmc, mc);
        hmc.getCommandLine().setCommandContext(context);
        hmc.getCommandLine().setBaseContext(context);
        if (hmc.getConfig().get(ACCOUNT_REFRESH_ENABLED, true)) {
            context.getAccountRefreshingService().start(hmc.getConfig(), mc);
        }
    }

    @Override
    protected void readCommandLine(HeadlessMc hmc) {
        if (hmc.getConfig().get(DEBUG_JLINE, false)) {
            hmc.getLoggingService().setLevel(Level.ALL);
            hmc.getLoggingService().setFileHandlerLogLevel(Level.ALL);
            // do we really have to do this? its kinda odd
            Logger.getLogger("org.jline").setLevel(Level.ALL);
            Arrays.stream(Logger.getLogger("org.jline").getHandlers()).forEach(handler -> handler.setLevel(Level.ALL));
        }

        HMCLog4JAppender.closeTerminalConsoleAppender(hmc.getDeencapsulator());
        Object remoteCommandLine = ApiClassloadingHelper.installOnOtherInstances(hmc);
        if (remoteCommandLine == null) {
            if (hmc.getConfig().get(JLineProperties.ENABLED, true)) {
                VersionAgnosticJLineCommandLineReader.installAndReadAsyncAndAwaitException(mc, hmc);
            } else {
                hmc.getCommandLine().readAsync(hmc);
            }
        } else {
            HMCLog4JAppender.install(remoteCommandLine);
        }

        /* Code to test if the appender works
        Thread annoyingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    System.out.println("SysOut");
                    LogManager.getLogger("Test").info("TestLogger!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        annoyingThread.setDaemon(true);
        annoyingThread.start();*/
    }

}
