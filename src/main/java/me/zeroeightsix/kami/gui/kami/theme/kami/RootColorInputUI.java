package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorInput;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class RootColorInputUI extends AbstractComponentUI<ColorInput> {

    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    @Override
    public void handleAddComponent(ColorInput component, Container container) {
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximumWidth() + "") + 3);
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
    }
}
