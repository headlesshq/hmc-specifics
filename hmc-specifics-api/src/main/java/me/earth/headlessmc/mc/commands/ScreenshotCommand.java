package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.screenshot.Screenshots;
import picocli.CommandLine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Not a {@link ScheduledCommand}, {@link Screenshots} schedules the screenshot on the main thread itself
 * and this command waits for it on the command thread.
 */
@CommandLine.Command(name = "screenshot", description = "Takes a screenshot, if the game renders (not in HeadlessMc's headless mode).")
public class ScreenshotCommand extends AbstractMinecraftCommand {
    @CommandLine.Parameters(index = "0", arity = "0..1", paramLabel = "<file>", description = "The PNG file to write, screenshots/hmc_<time>.png by default.")
    private File file;

    @CommandLine.Option(names = "--timeout", description = "How long to wait for the screenshot in seconds, 10 by default.")
    private long timeout = 10L;

    public ScreenshotCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        File target = file == null
            ? new File("screenshots", "hmc_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".png")
            : file;
        try {
            File written = Screenshots.take(mc, target).get(timeout, TimeUnit.SECONDS);
            ctx.log("Saved screenshot to " + written.getAbsolutePath());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new CommandException("Failed to take screenshot: " + cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new CommandException("Timed out waiting for the screenshot after " + timeout + "s.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Interrupted while waiting for the screenshot.");
        }
    }

}
