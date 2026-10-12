package me.earth.headlessmc.mc;

import me.earth.headlessmc.api.config.Property;

import static me.earth.headlessmc.api.config.PropertyTypes.bool;
import static me.earth.headlessmc.api.config.PropertyTypes.number;

public interface SpecificProperties {
    Property<Boolean> KEEP_RUNTIME_COMMANDS = bool("hmc.keepruntimecommands");
    Property<Boolean> DEBUG_JLINE = bool("hmc.debug.jline");

    Property<Boolean> ACCOUNT_REFRESH_ENABLED = bool("hmc.account.refresh.enabled");
    Property<Long> REFRESH_INTERVAL = number("hmc.account.refresh.interval");

    Property<Boolean> AUTO_ACCEPT_SERVER_RESOURCE_PACKS = bool("hmc.resourcepacks.autoaccept");

}
