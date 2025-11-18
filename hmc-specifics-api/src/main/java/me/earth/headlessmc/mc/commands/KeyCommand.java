package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.api.HasId;
import me.earth.headlessmc.api.HasName;
import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.command.CommandException;
import me.earth.headlessmc.api.command.CommandUtil;
import me.earth.headlessmc.api.command.ParseUtil;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.keyboard.HmcKeyboard;
import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.keyboard.RobotKeyboard;
import me.earth.headlessmc.mc.util.ExtendedTable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("KeyCommand-Thread");
        thread.setDaemon(true);
        return thread;
    });

    public KeyCommand(HeadlessMc ctx, Minecraft mc) {
        super(ctx, mc, "key", "Allows you to press keys.");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        Keyboard keyboard = CommandUtil.hasFlag("-robot", args)
                                || CommandUtil.hasFlag("-robot-list", args)
                ? new RobotKeyboard()
                : HmcKeyboard.getInstance().getMcKeyboard(mc);

        if (args.length <= 1 || CommandUtil.hasFlag("-robot-list", args)) {
            ctx.log(new ExtendedTable<Key>()
                    .addAll(keyboard)
                    .withInt("id", Key::getId)
                    .withColumn("name", Key::getName)
                    .build());
            return;
        }

        Key key;
        if (CommandUtil.hasFlag("-custom", args)) {
            if (args.length < 4) {
                throw new CommandException("Please specify name, id, and scancode.");
            }

            String name = args[1];
            int id = ParseUtil.parseI(args[2]);
            int scanCode = ParseUtil.parseI(args[3]);
            key = new Key(name, id, scanCode);
        } else {
            key = CommandUtil.hasFlag("-id", args)
                    ? HasId.getById(ParseUtil.parseI(args[1]), keyboard)
                    : HasName.getByName(args[1], keyboard);
        }

        if (key == null) {
            throw new CommandException("Failed to find key: " + args[1]);
        }

        String durationArg = CommandUtil.getOption("--duration", args);
        boolean indefinite = CommandUtil.hasFlag("-indefinite", args);
        long duration = durationArg == null ? 80 : ParseUtil.parseL(durationArg, 0, Long.MAX_VALUE);
        boolean release = CommandUtil.hasFlag("-release", args);
        if (release) {
            keyboard.release(key);
        } else {
            keyboard.press(key);
        }

        if (!indefinite && !release) {
            executor.schedule(() -> {
                mc.schedule(() -> keyboard.release(key));
            }, duration, TimeUnit.MILLISECONDS);
        }
    }

}
