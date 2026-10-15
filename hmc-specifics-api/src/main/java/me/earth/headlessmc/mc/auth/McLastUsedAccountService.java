package me.earth.headlessmc.mc.auth;

import io.github.headlesshq.headlessmc.auth.Account;
import io.github.headlesshq.headlessmc.auth.AuthException;
import io.github.headlesshq.headlessmc.auth.LastUsedAccountService;
import me.earth.headlessmc.mc.Minecraft;

import java.util.Optional;

/**
 * The HeadlessMC account commands (login, select, refresh) call {@link #setLastUsedAccount(Account)}.
 * We use that to also switch the account Minecraft is using.
 */
public class McLastUsedAccountService implements LastUsedAccountService {
    private final LastUsedAccountService delegate;
    private final AccountRefreshingService accountRefreshingService;
    private final Minecraft mc;

    public McLastUsedAccountService(LastUsedAccountService delegate,
                                    AccountRefreshingService accountRefreshingService,
                                    Minecraft mc) {
        this.delegate = delegate;
        this.accountRefreshingService = accountRefreshingService;
        this.mc = mc;
    }

    @Override
    public Optional<Account> getLastUsedAccount() throws AuthException {
        return delegate.getLastUsedAccount();
    }

    @Override
    public void setLastUsedAccount(Account account) throws AuthException {
        delegate.setLastUsedAccount(account);
        accountRefreshingService.setAccount(account);
        McAccount mcAccount = HmcAuth.toMcAccount(account);
        mc.schedule(() -> mc.setMcAccount(mcAccount));
    }

}
