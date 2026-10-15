package me.earth.headlessmc.testmod;

/**
 * A mod that adds commands to HMC-Specifics, used by the in-game tests in CI, see hmc-specifics-test.json.
 * It is not a dependency of HMC-Specifics, it only uses the API like any other mod would.
 * The entrypoints for the different modloaders only log that the mod has been loaded.
 */
public final class TestMod {
    public static final String ID = "hmcs_test_mod";
    public static final String NAME = "HMC-Specifics Test Mod";
    public static final String VERSION = "1.0.0";
    public static final String PREFIX = "[hmcs-test] ";

    private TestMod() {
        throw new AssertionError();
    }

    static void loaded(String loader) {
        System.out.println(PREFIX + NAME + " loaded on " + loader);
    }

}
