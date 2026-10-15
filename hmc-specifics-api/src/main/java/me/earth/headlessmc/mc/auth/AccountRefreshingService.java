package me.earth.headlessmc.mc.auth;

import io.github.headlesshq.headlessmc.auth.Account;
import io.github.headlesshq.headlessmc.auth.AuthService;
import me.earth.headlessmc.mc.Minecraft;
import me.earth.headlessmc.mc.SpecificProperties;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Periodically refreshes the account Minecraft is using with the HeadlessMC {@link AuthService},
 * as Minecraft tokens expire after 24 hours.
 */
public class AccountRefreshingService {
    private static final Logger LOGGER = Logger.getLogger(AccountRefreshingService.class.getName());

    private final AuthService authService;
    private final Minecraft mc;

    private volatile ScheduledExecutorService executor;
    private volatile Future<?> future;
    private volatile Account account;

    public AccountRefreshingService(AuthService authService, Minecraft mc) {
        this.authService = authService;
        this.mc = mc;
    }

    public boolean isRunning() {
        return executor != null;
    }

    public synchronized void setAccount(Account account) {
        this.account = account;
    }

    public synchronized boolean start() {
        if (executor == null) {
            LOGGER.info("Starting account refreshing service");
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("AccountRefreshingService");
                return thread;
            });

            schedule(executor);
            return true;
        }

        return false;
    }

    public synchronized boolean stop() {
        if (executor != null) {
            if (future != null) {
                future.cancel(true);
            }

            executor.shutdownNow();
            executor = null;
            future = null;
            return true;
        }

        return false;
    }

    public synchronized boolean refresh() {
        try {
            Account account = findAccount();
            if (account == null) {
                LOGGER.severe("Failed to refresh account, failed to find the current account in " + HmcAuth.getAuthDir());
                return false;
            }

            Account refreshed = authService.refresh(account);
            this.account = refreshed;
            McAccount mcAccount = HmcAuth.toMcAccount(refreshed);
            mc.schedule(() -> mc.setMcAccount(mcAccount));
            LOGGER.info("Refreshed account successfully.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to refresh account", e);
            return false;
        } finally {
            ScheduledExecutorService executor = this.executor;
            if (executor != null) {
                schedule(executor);
            }
        }
    }

    private Account findAccount() {
        McAccount mcAccount = mc.getMcAccount();
        Account account = this.account;
        if (account != null && HmcAuth.isSameAccount(account, mcAccount)) {
            return account;
        }

        for (Account candidate : authService.getAccounts()) {
            if (HmcAuth.isSameAccount(candidate, mcAccount)) {
                return candidate;
            }
        }

        return null;
    }

    private void schedule(ScheduledExecutorService executor) {
        Future<?> previous = future;
        if (previous != null) { // refresh might have been called manually, do not schedule twice
            previous.cancel(false);
        }

        long interval = SpecificProperties.getRefreshIntervalSeconds();
        future = executor.schedule(this::refresh, interval, TimeUnit.SECONDS);
    }

}
