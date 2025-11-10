package me.earth.headlessmc.mc.adventure;

import me.earth.headlessmc.mc.util.ExtendedTable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdventureHelperTest {
    @Test
    @Disabled
    public void test() {
        String json = "{\"text\":\"\",\"extra\":[{\"translate\":\"item.minecraft.diamond_sword\"}],\"color\":\"aqua\"}";
        AdventureHelper helper = AdventureWrapper.getAdventureHelper(s -> false, (s, s2) -> s);
        assertNotNull(helper);
        String ansiString = helper.toAnsiString(json);
        assertEquals("\u001B[m\u001B[96mitem.minecraft.diamond_sword\u001B[0m", ansiString);

        ExtendedTable<String> table = new ExtendedTable<String>()
                .withInt("length", String::length)
                .withColumn("text", Function.identity())
                .withInt("length", String::length);
        table.add("item.minecraft.diamond_sword").add(ansiString);

        assertEquals("length   text                           length\n" +
                "28       item.minecraft.diamond_sword   28\n" +
                "40       \u001B[m\u001B[96mitem.minecraft.diamond_sword\u001B[0m   40", table.buildCalculatingAnsiWidth());
    }

}
