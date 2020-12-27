package me.zeroeightsix.kami.gui.clickgui

import me.zeroeightsix.kami.gui.AbstractKamiGui
import me.zeroeightsix.kami.gui.clickgui.component.ModuleButton
import me.zeroeightsix.kami.gui.clickgui.window.ModuleSettingWindow
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard

object KamiClickGui : AbstractKamiGui<ModuleSettingWindow, Module>() {

    init {
        val allButtons = ModuleManager.getModules().map { ModuleButton(it) }
        var posX = 10.0f

        for (category in Module.Category.values()) {
            val buttons = allButtons.filter { it.module.category == category }.toTypedArray()
            if (buttons.isNullOrEmpty()) continue
            windowList.add(ListWindow(category.displayName, posX, 10.0f, 100.0f, 256.0f, Component.SettingGroup.CLICK_GUI, *buttons))
            posX += 110.0f
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setModuleVisibility { true }
    }

    override fun newSettingWindow(element: Module, mousePos: Vec2f): ModuleSettingWindow {
        return ModuleSettingWindow(element, mousePos.x, mousePos.y)
    }


    override fun handleKeyboardInput() {
        super.handleKeyboardInput()
        val keyCode = Keyboard.getEventKey()

        if (settingWindow?.listeningChild == null && (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE)) {
            typedString = ""
            lastTypedTime = 0L
            stringWidth = 0.0f
            prevStringWidth = 0.0f

            setModuleVisibility { true }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || ClickGUI.bind.value.isDown(keyCode)) {
            ClickGUI.disable()
        } else if (settingWindow?.listeningChild == null) {
            when {
                typedChar.isLetter() || typedChar == ' ' -> {
                    typedString += typedChar
                    stringWidth = FontRenderAdapter.getStringWidth(typedString, 1.666f)
                    lastTypedTime = System.currentTimeMillis()

                    val string = typedString.replace(" ", "")
                    setModuleVisibility { moduleButton ->
                        moduleButton.module.alias.any { it.contains(string, true) }
                    }
                }
            }
        }
    }

    private fun setModuleVisibility(function: (ModuleButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach {
            for (child in it.children) {
                if (child !is ModuleButton) continue
                child.visible.value = function(child)
            }
        }
    }
}