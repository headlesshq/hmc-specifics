package me.earth.headlessmc.mc.commands;

import io.github.headlesshq.headlessmc.commands.auth.AccountCommand;
import io.github.headlesshq.headlessmc.commands.auth.AccountCompletions;
import io.github.headlesshq.headlessmc.commands.auth.AuthProviderCompletions;
import io.github.headlesshq.headlessmc.console.format.SimpleTable;
import io.github.headlesshq.headlessmc.console.format.TableBuilder;
import io.github.headlesshq.headlessmc.console.format.TableProvider;

/**
 * HeadlessMC creates its auth commands and completion candidates with CDI.
 * We do not have CDI, so these {@link CommandFactory}s supply their dependencies.
 */
public final class AuthCommandFactories {
    private AuthCommandFactories() {
        throw new AssertionError();
    }

    public static final class AccountCommandFactory implements CommandFactory<AccountCommand> {
        @Override
        public Class<AccountCommand> getType() {
            return AccountCommand.class;
        }

        @Override
        public AccountCommand create(MinecraftContext ctx) {
            return new AccountCommand(
                ctx.getLastUsedAccountService(), new SimpleTableProvider(), ctx.getAuthService(), ctx.getConsole()
            );
        }
    }

    public static final class AccountCompletionsFactory implements CommandFactory<AccountCompletions> {
        @Override
        public Class<AccountCompletions> getType() {
            return AccountCompletions.class;
        }

        @Override
        public AccountCompletions create(MinecraftContext ctx) {
            return new AccountCompletions(ctx.getAuthService());
        }
    }

    public static final class AuthProviderCompletionsFactory implements CommandFactory<AuthProviderCompletions> {
        @Override
        public Class<AuthProviderCompletions> getType() {
            return AuthProviderCompletions.class;
        }

        @Override
        public AuthProviderCompletions create(MinecraftContext ctx) {
            return new AuthProviderCompletions(ctx.getAuthService());
        }
    }

    private static final class SimpleTableProvider implements TableProvider {
        @Override
        public <T> TableBuilder<T> get() {
            return new SimpleTable<>();
        }
    }

}
