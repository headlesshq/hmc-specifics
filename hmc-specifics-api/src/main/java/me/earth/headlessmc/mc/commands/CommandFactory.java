package me.earth.headlessmc.mc.commands;

import io.github.headlesshq.headlessmc.auth.AuthService;
import picocli.CommandLine;

import java.lang.reflect.Constructor;

/**
 * HeadlessMC creates commands and completion candidates with CDI.
 * We do not have CDI, so this factory supplies the {@link AuthService}
 * to classes that require it, e.g. {@code AccountCompletions}.
 */
public class CommandFactory implements CommandLine.IFactory {
    private final CommandLine.IFactory defaultFactory = CommandLine.defaultFactory();
    private final AuthService authService;

    public CommandFactory(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            Constructor<K> constructor = cls.getDeclaredConstructor(AuthService.class);
            constructor.setAccessible(true);
            return constructor.newInstance(authService);
        } catch (NoSuchMethodException e) {
            return defaultFactory.create(cls);
        }
    }

}
