package me.earth.headlessmc.mc;

import net.minecraft.util.FormattedCharSequence;

public final class CharSinkUtil {
    private CharSinkUtil() {
        throw new AssertionError();
    }

    public static String toString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((i, s, ch) -> {
            sb.append((char) ch);
            return true;
        });
        return sb.toString();
    }

}
