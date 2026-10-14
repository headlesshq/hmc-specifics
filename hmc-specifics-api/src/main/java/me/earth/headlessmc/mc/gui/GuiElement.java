package me.earth.headlessmc.mc.gui;

import me.earth.headlessmc.mc.util.HasId;
import me.earth.headlessmc.mc.Adapter;

import java.util.Collections;
import java.util.List;

/**
 * Represents a GuiElement which belongs to a {@link GuiScreen}.
 */
public interface GuiElement extends HasId, Adapter {
    /**
     * @return the text of this element.
     */
    String getText();

    /**
     * @return the x position of this element.
     */
    int getX();

    /**
     * @return the y position of this element.
     */
    int getY();

    /**
     * @return the width of this element.
     */
    int getWidth();

    /**
     * @return the height of this element.
     */
    int getHeight();

    /**
     * @return Ansi String with tool tip
     */
    default List<String> getTooltip() {
        return Collections.emptyList();
    }

}
