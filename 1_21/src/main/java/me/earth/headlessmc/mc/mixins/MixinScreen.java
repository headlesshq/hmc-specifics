package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.ComponentHelper;
import me.earth.headlessmc.mc.IdManager;
import me.earth.headlessmc.mc.ReflectionHelper;
import me.earth.headlessmc.mc.gui.GuiButton;
import me.earth.headlessmc.mc.gui.GuiElement;
import me.earth.headlessmc.mc.gui.GuiScreen;
import me.earth.headlessmc.mc.gui.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(Screen.class)
public abstract class MixinScreen implements GuiScreen, GuiEventListener {
    @Final
    @Shadow
    private static Logger LOGGER;
    @Final
    @Shadow
    private List<Renderable> renderables;
    @Shadow
    private Minecraft minecraft;

    @Override
    public void click(int x, int y, int button) {
        mouseMoved(x, y);
        mouseClicked(x, y, button);
        mouseReleased(x, y, button);
    }

    @Override
    public List<GuiButton> getButtons() {
        Set<GuiButton> buttons = new LinkedHashSet<>();
        renderables.stream()
                   .filter(GuiButton.class::isInstance)
                   .map(GuiButton.class::cast)
                   .forEach(buttons::add);
        buttons.addAll(ReflectionHelper.findAll(this, GuiButton.class));
        return new ArrayList<>(buttons);
    }

    @Override
    public List<TextField> getTextFields() {
        Set<TextField> textFields = new LinkedHashSet<>();
        renderables.stream()
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .forEach(textFields::add);
        textFields.addAll(ReflectionHelper.findAll(this, TextField.class));
        return new ArrayList<>(textFields);
    }

    @Override
    public List<GuiElement> getAllElements() {
        Set<GuiElement> result = new LinkedHashSet<>();
        renderables.stream()
                   .filter(GuiElement.class::isInstance)
                   .map(GuiElement.class::cast)
                   .forEach(result::add);
        result.addAll(ReflectionHelper.findAll(this, GuiElement.class));
        if (this instanceof MenuAccess<?> menuAccess) {
            AbstractContainerMenu menu = menuAccess.getMenu();
            for (Slot slot : menu.slots) {
                ItemStack stack = slot.getItem();
                String text = "";
                if (!stack.isEmpty()) {
                    text = ComponentHelper.toAnsiStringOrContent(stack.getDisplayName());
                }

                int xOffset = 0;
                int yOffset = 0;
                //noinspection ConstantValue
                if (AbstractContainerScreen.class.isInstance(this)) {
                    AbstractContainerScreen<?> acs = AbstractContainerScreen.class.cast(this);
                    IAbstractContainerScreen accessor = (IAbstractContainerScreen) acs;
                    xOffset = (acs.width - accessor.getImageWidth()) / 2;
                    yOffset = (acs.height - accessor.getImageHeight()) / 2;
                }

                List<String> tooltip = hmc$GetTooltip(stack);
                result.add(new me.earth.headlessmc.mc.gui.Slot(
                        text,
                        slot.x + xOffset,
                        slot.y + yOffset,
                        18, //AbstractContainerMenu.SLOT_SIZE,
                        18, //AbstractContainerMenu.SLOT_SIZE,
                        slot.index,
                        tooltip
                ));
            }

            ItemStack carried = menu.getCarried();
            if (!carried.isEmpty()) {
                String text = "";
                if (!carried.isEmpty()) {
                    text = ComponentHelper.toAnsiStringOrContent(carried.getDisplayName());
                }

                List<String> tooltip = hmc$GetTooltip(carried);
                result.add(new me.earth.headlessmc.mc.gui.Carried(
                        text,
                        0,
                        0,
                        18, //AbstractContainerMenu.SLOT_SIZE,
                        18, // AbstractContainerMenu.SLOT_SIZE,
                        result.size(),
                        tooltip
                ));
            }
        }

        return new ArrayList<>(result);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initHook(Component component, CallbackInfo ci) {
        IdManager.INSTANCE.setContext(this);
    }

    private List<String> hmc$GetTooltip(ItemStack stack) {
        Minecraft mc = minecraft;
        if (mc == null) {
            mc = Minecraft.getInstance();
        }

        List<String> tooltip = new ArrayList<>();
        try {
            for (Component component : Screen.getTooltipFromItem(mc, stack)) {
                tooltip.add(ComponentHelper.toAnsiStringOrContent(component));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get tooltip", e);
        }

        return tooltip;
    }

}
