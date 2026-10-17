package me.earth.headlessmc.mc.test;

import me.earth.headlessmc.mc.CharSinkUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCharSinkUtil {
    @Test
    public void testTextComponent() {
        String expected = "Test";
        Component component = new TextComponent(expected);
        String string = CharSinkUtil.toString(component.getVisualOrderText());
        Assertions.assertEquals(expected, string);
    }

}
