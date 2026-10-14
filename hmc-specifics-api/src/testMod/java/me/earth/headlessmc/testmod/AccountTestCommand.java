package me.earth.headlessmc.testmod;

import picocli.CommandLine;

/**
 * A subcommand of the account command of HMC-Specifics.
 * Has a no-arg constructor and writes to the output picocli has been configured with.
 */
@CommandLine.Command(name = "hmcs-test", description = "Account subcommand of the HMC-Specifics test mod.")
public class AccountTestCommand implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().getOut().println(TestMod.PREFIX + "Account subcommand executed.");
    }

}
