package me.earth.headlessmc.mc.commands;

/**
 * Creates instances of a class that picocli needs, e.g. a command, a subcommand or {@code completionCandidates}.
 * Most classes do not need a factory: classes with a constructor that takes a {@link MinecraftContext}
 * or with a no-arg constructor are instantiated automatically.
 * Factories are loaded with a {@link java.util.ServiceLoader},
 * register them in {@code META-INF/services/me.earth.headlessmc.mc.commands.CommandFactory}.
 *
 * @param <T> the type of the instances created by this factory.
 * @see CommandRegistry
 */
public interface CommandFactory<T> {
    /**
     * @return the class this factory creates instances of.
     */
    Class<T> getType();

    /**
     * Creates a new instance. Called every time a command is executed or completed,
     * because picocli stores the parsed arguments inside the command objects.
     *
     * @param ctx the context to get dependencies from.
     * @return a new instance of {@link #getType()}.
     * @throws Exception if the instance could not be created.
     */
    T create(MinecraftContext ctx) throws Exception;

}
