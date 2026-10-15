package me.earth.headlessmc.mc.commands;

import picocli.CommandLine;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Instantiates classes for picocli. Uses a registered {@link CommandFactory} if there is one,
 * otherwise a constructor that takes a {@link MinecraftContext}, otherwise picocli's default factory.
 */
public class PicocliFactory implements CommandLine.IFactory {
    private final CommandLine.IFactory defaultFactory = CommandLine.defaultFactory();
    private final Map<Class<?>, CommandFactory<?>> factories;
    private final MinecraftContext ctx;

    public PicocliFactory(MinecraftContext ctx, Map<Class<?>, CommandFactory<?>> factories) {
        this.ctx = ctx;
        this.factories = factories;
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        CommandFactory<?> factory = factories.get(cls);
        if (factory != null) {
            return cls.cast(factory.create(ctx));
        }

        Constructor<K> constructor;
        try {
            constructor = cls.getDeclaredConstructor(MinecraftContext.class);
        } catch (NoSuchMethodException e) {
            return defaultFactory.create(cls);
        }

        constructor.setAccessible(true);
        return constructor.newInstance(ctx);
    }

}
