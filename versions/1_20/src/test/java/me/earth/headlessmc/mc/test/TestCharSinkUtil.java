package me.earth.headlessmc.mc.test;

import me.earth.headlessmc.mc.CharSinkUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCharSinkUtil {
    @Test
    public void testTextComponent() {
        String expected = "Test";
        FormattedCharSequence seq =
            FormattedCharSequence.forward(expected, Style.EMPTY);
        String string = CharSinkUtil.toString(seq);
        Assertions.assertEquals(expected, string);
    }

}
