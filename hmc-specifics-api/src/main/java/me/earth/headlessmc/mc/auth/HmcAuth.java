package me.earth.headlessmc.mc.auth;

import com.google.gson.reflect.TypeToken;
import io.github.headlesshq.headlessmc.auth.Account;
import io.github.headlesshq.headlessmc.auth.AccountInfo;
import io.github.headlesshq.headlessmc.auth.AuthProvider;
import io.github.headlesshq.headlessmc.auth.AuthService;
import io.github.headlesshq.headlessmc.auth.AuthServiceImpl;
import io.github.headlesshq.headlessmc.auth.AuthStore;
import io.github.headlesshq.headlessmc.auth.LastUsedAccountService;
import io.github.headlesshq.headlessmc.auth.LastUsedAccountServiceImpl;
import io.github.headlesshq.headlessmc.auth.impl.McAuthProvider;
import io.github.headlesshq.headlessmc.auth.offline.OfflineAuthProvider;
import io.github.headlesshq.headlessmc.auth.store.gson.GsonFileAuthStore;
import io.github.headlesshq.headlessmc.auth.store.gson.GsonCodecImpl;
import me.earth.headlessmc.mc.SpecificProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the HeadlessMC authentication services,
 * using the same files as the HeadlessMC launcher (see HeadlessMC's AuthFactory and AppFiles).
 */
public class HmcAuth {
    private static final Logger LOGGER = Logger.getLogger(HmcAuth.class.getName());

    private final AuthService authService;
    private final LastUsedAccountService lastUsedAccountService;

    public HmcAuth(Path authDir) {
        AuthProvider defaultProvider = McAuthProvider.of(
            AuthProvider.DEFAULT,
            null,
            authDir.resolve(AuthProvider.DEFAULT).resolve(".accounts.json")
        );

        AuthStore<Account> offlineStore = new GsonFileAuthStore<>(
            new GsonCodecImpl<>(Account.class),
            new ReentrantLock(),
            OfflineAuthProvider.VERSION,
            authDir.resolve(AuthProvider.OFFLINE).resolve(".accounts.json")
        );

        AuthProvider offlineProvider = new OfflineAuthProvider(
            offlineStore,
            throwable -> LOGGER.log(Level.SEVERE, "Offline auth", throwable),
            AuthProvider.OFFLINE,
            OfflineAuthProvider.VERSION
        );

        this.authService = new AuthServiceImpl(Arrays.asList(defaultProvider, offlineProvider), defaultProvider);
        //noinspection Convert2Diamond
        AuthStore<List<AccountInfo>> lastUsedStore = new GsonFileAuthStore<>(
            new GsonCodecImpl<>(new TypeToken<List<AccountInfo>>() {}),
            new ReentrantLock(),
            LastUsedAccountServiceImpl.VERSION,
            authDir.resolve("last").resolve(".accounts.json")
        );

        this.lastUsedAccountService = new LastUsedAccountServiceImpl(
            lastUsedStore,
            authService,
            (message, throwable) -> LOGGER.log(Level.SEVERE, message, throwable)
        );
    }

    public AuthService getAuthService() {
        return authService;
    }

    public LastUsedAccountService getLastUsedAccountService() {
        return lastUsedAccountService;
    }

    public static McAccount toMcAccount(Account account) {
        return new McAccount(account.getName(), parseUuid(account.getUuid()), account.getToken());
    }

    public static boolean isSameAccount(Account account, McAccount mcAccount) {
        return mcAccount != null && parseUuid(account.getUuid()).equals(mcAccount.getUuid());
    }

    public static UUID parseUuid(String uuid) {
        String withoutDashes = uuid.replace("-", "").toLowerCase(Locale.ENGLISH);
        if (withoutDashes.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID " + uuid);
        }

        return new UUID(
            Long.parseUnsignedLong(withoutDashes.substring(0, 16), 16),
            Long.parseUnsignedLong(withoutDashes.substring(16), 16)
        );
    }

    /**
     * @return the directory HeadlessMC stores accounts in.
     */
    public static Path getAuthDir() {
        return getDataDir().resolve(".auth");
    }

    private static Path getDataDir() {
        String location = System.getProperty(SpecificProperties.FILES_LOCATION);
        if (location == null || location.trim().isEmpty()) {
            location = System.getenv("HMC_FILES_LOCATION");
        }

        if (location != null && !location.trim().isEmpty()) {
            return Paths.get(location);
        }

        Path userHome = Paths.get(System.getProperty("user.home", ""));
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        Path fallback;
        if (os.contains("windows")) {
            fallback = fromEnv("APPDATA", userHome.resolve("AppData").resolve("Roaming"));
        } else if (os.contains("mac")) {
            fallback = userHome.resolve("Library").resolve("Application Support");
        } else {
            fallback = userHome.resolve(".local").resolve("share");
        }

        return fromEnv("XDG_DATA_HOME", fallback).resolve("headlessmc");
    }

    private static Path fromEnv(String variable, Path fallback) {
        String value = System.getenv(variable);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        Path path = Paths.get(value);
        return path.isAbsolute() ? path : fallback;
    }

}
