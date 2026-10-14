package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.keyboard.HmcKeyboard;
import me.earth.headlessmc.mc.keyboard.Key;
import me.earth.headlessmc.mc.keyboard.Keyboard;
import me.earth.headlessmc.mc.keyboard.RobotKeyboard;
import me.earth.headlessmc.mc.util.ExtendedTable;
import me.earth.headlessmc.mc.util.HasId;
import me.earth.headlessmc.mc.util.HasName;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
    name = "key",
    description = "Allows you to press keys.",
    customSynopsis = "key [<name>] [-id] [-robot] [-robot-list] [-release] [-indefinite] [--duration <ms>]%n" +
        "   or: key <name> <id> <scancode> -custom"
)
public class KeyCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("KeyCommand-Thread");
        thread.setDaemon(true);
        return thread;
    });

    @CommandLine.Parameters(arity = "0..3", paramLabel = "<key>", description = "The name (or id with -id) of the key to press. Lists all keys if not specified.")
    private List<String> params = new ArrayList<>();

    @CommandLine.Option(names = "-robot", description = "Uses java.awt.Robot to press the key.")
    private boolean robot;

    @CommandLine.Option(names = "-robot-list", description = "Lists the keys of java.awt.Robot.")
    private boolean robotList;

    @CommandLine.Option(names = "-custom", description = "Presses a key with a custom name, id and scancode.")
    private boolean custom;

    @CommandLine.Option(names = "-id", description = "Finds the key by its id instead of its name.")
    private boolean id;

    @CommandLine.Option(names = "--duration", paramLabel = "<ms>", description = "How long to press the key, 80ms by default.")
    private long duration = 80;

    @CommandLine.Option(names = "-indefinite", description = "Does not release the key.")
    private boolean indefinite;

    @CommandLine.Option(names = "-release", description = "Releases the key.")
    private boolean release;

    public KeyCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        Keyboard keyboard = robot || robotList
                ? new RobotKeyboard()
                : HmcKeyboard.getInstance().getMcKeyboard(mc);

        if (params.isEmpty() || robotList) {
            ctx.log(new ExtendedTable<Key>()
                    .addAll(keyboard)
                    .withInt("id", Key::getId)
                    .withColumn("name", Key::getName)
                    .build());
            return;
        }

        Key key;
        if (custom) {
            if (params.size() < 3) {
                throw new CommandException("Please specify name, id, and scancode.");
            }

            key = new Key(params.get(0), parseInt(params.get(1)), parseInt(params.get(2)));
        } else {
            key = id
                    ? HasId.getById(parseInt(params.get(0)), keyboard)
                    : HasName.getByName(params.get(0), keyboard);
        }

        if (key == null) {
            throw new CommandException("Failed to find key: " + params.get(0));
        }

        if (duration < 0) {
            throw new CommandException("Duration must be >= 0!");
        }

        if (release) {
            keyboard.release(key);
        } else {
            keyboard.press(key);
        }

        if (!indefinite && !release) {
            EXECUTOR.schedule(() -> {
                mc.schedule(() -> keyboard.release(key));
            }, duration, TimeUnit.MILLISECONDS);
        }
    }

    private static int parseInt(String value) throws CommandException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CommandException("Failed to parse '" + value + "' as a number!");
        }
    }

}
