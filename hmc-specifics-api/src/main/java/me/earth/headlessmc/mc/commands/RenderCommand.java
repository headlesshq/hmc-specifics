package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.gui.FontRenderer;
import me.earth.headlessmc.mc.gui.FontRendererListener;
import picocli.CommandLine;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@CommandLine.Command(name = "render", description = "Outputs all text rendered on the screen.")
public class RenderCommand extends AbstractMinecraftCommand {
    @CommandLine.Parameters(index = "0", arity = "0..1", paramLabel = "<time>", description = "How long to listen in milliseconds, 100 by default.")
    private long time = 100L;

    @CommandLine.Option(names = "-f", description = "Do not filter out duplicate texts.")
    private boolean noFilter;

    @CommandLine.Option(names = "-t", description = "Listen on the current thread.")
    private boolean currentThread;

    public RenderCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        if (time < 0) {
            throw new CommandException("Time must be > 0!");
        }

        FontRenderer fontRenderer = mc.getFontRenderer();
        boolean noFilter = this.noFilter;
        Set<String> f = Collections.newSetFromMap(new ConcurrentHashMap<>());
        FontRendererListener listener = (text, x, y) -> {
            if (noFilter || f.add(text)) {
                ctx.log(String.format("{x=%s, y=%s, text=%s}", x, y, text));
            }
        };

        Runnable stopListening = stopListening(fontRenderer, listener, time);
        if (currentThread) {
            stopListening.run();
        } else {
            Thread thread = new Thread(stopListening);
            thread.setDaemon(true);
            thread.setName(String.format("RenderListener Exit %sms", time));
            thread.start();
        }
    }

    private Runnable stopListening(FontRenderer fontRenderer,
                                   FontRendererListener listener,
                                   long time) {
        return () -> {
            try {
                fontRenderer.register(listener);
                Thread.sleep(time);
            } catch (InterruptedException e) {
                ctx.log(Thread.currentThread().getName() + " has been interrupted!");
                Thread.currentThread().interrupt();
            } finally {
                fontRenderer.unregister(listener);
            }
        };
    }

}
