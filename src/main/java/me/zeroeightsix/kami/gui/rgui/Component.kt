package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.HAlign
import me.zeroeightsix.kami.util.graphics.font.VAlign
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.max

open class Component(
    name: String,
    posXIn: Float,
    posYIn: Float,
    widthIn: Float,
    heightIn: Float,
    val settingGroup: SettingGroup
) {

    // Basic info
    val originalName = name
    val name = setting("Name", name, { false })
    val visible = setting("Visible", true, { false }, { _, it -> it || !closeable })

    protected val relativePosX = setting("PosX", posXIn, -69420.911f..69420.911f, 0.1f, { false },
        { _, it -> if (this is WindowComponent && KamiMod.isReady()) absToRelativeX(relativeToAbsX(it).coerceIn(2.0f, max(scaledWidth - width.value - 2.0f, 2.069f))) else it })
    protected val relativePosY = setting("PosY", posYIn, -69420.911f..69420.911f, 0.1f, { false },
        { _, it -> if (this is WindowComponent && KamiMod.isReady()) absToRelativeY(relativeToAbsY(it).coerceIn(2.0f, max(scaledHeight - height.value - 2.0f, 2.069f))) else it })

    val width = setting("Width", widthIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(minWidth, scaledWidth) })
    val height = setting("Height", heightIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(minHeight, scaledHeight) })

    val dockingH = setting("DockingH", HAlign.LEFT)
    val dockingV = setting("DockingV", VAlign.TOP)

    var posX: Float
        get() {
            return relativeToAbsX(relativePosX.value)
        }
        set(value) {
            if (!KamiMod.isReady()) return
            relativePosX.value = absToRelativeX(value)
        }

    var posY: Float
        get() {
            return relativeToAbsY(relativePosY.value)
        }
        set(value) {
            if (!KamiMod.isReady()) return
            relativePosY.value = absToRelativeY(value)
        }

    init {
        dockingH.listeners.add { posX = prevPosX }
        dockingV.listeners.add { posY = prevPosY }
    }

    // Extra info
    protected val mc = Wrapper.minecraft
    open val minWidth = 1.0f
    open val minHeight = 1.0f
    open val maxWidth = -1.0f
    open val maxHeight = -1.0f
    open val closeable: Boolean get() = true

    // Rendering info
    var prevPosX = 0.0f; protected set
    var prevPosY = 0.0f; protected set
    val renderPosX get() = prevPosX + prevDockWidth + (posX + dockWidth - (prevPosX + prevDockWidth)) * mc.renderPartialTicks - dockWidth
    val renderPosY get() = prevPosY + prevDockHeight + (posY + dockHeight - (prevPosY + prevDockHeight)) * mc.renderPartialTicks - dockHeight

    var prevWidth = 0.0f; protected set
    var prevHeight = 0.0f; protected set
    val renderWidth get() = prevWidth + (width.value - prevWidth) * mc.renderPartialTicks
    open val renderHeight get() = prevHeight + (height.value - prevHeight) * mc.renderPartialTicks

    private fun relativeToAbsX(xIn: Float) = xIn + scaledWidth * dockingH.value.multiplier - dockWidth
    private fun relativeToAbsY(yIn: Float) = yIn + scaledHeight * dockingV.value.multiplier - dockHeight
    private fun absToRelativeX(xIn: Float) = xIn - scaledWidth * dockingH.value.multiplier + dockWidth
    private fun absToRelativeY(yIn: Float) = yIn - scaledHeight * dockingV.value.multiplier + dockHeight

    protected val scaledWidth get() = mc.displayWidth / ClickGUI.getScaleFactorFloat()
    protected val scaledHeight get() = mc.displayHeight / ClickGUI.getScaleFactorFloat()
    private val dockWidth get() = width.value * dockingH.value.multiplier
    private val dockHeight get() = height.value * dockingV.value.multiplier
    private val prevDockWidth get() = prevWidth * dockingH.value.multiplier
    private val prevDockHeight get() = prevHeight * dockingV.value.multiplier

    // Update methods
    open fun onDisplayed() {}

    open fun onClosed() {}

    open fun onGuiInit() {
        updatePrevPos()
        updatePrevSize()
    }

    open fun onTick() {
        updatePrevPos()
        updatePrevSize()
    }

    private fun updatePrevPos() {
        prevPosX = posX
        prevPosY = posY
    }

    private fun updatePrevSize() {
        prevWidth = width.value
        prevHeight = height.value
    }

    open fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {}

    enum class SettingGroup(val groupName: String?) {
        NONE(null),
        CLICK_GUI("ClickGui"),
        HUD_GUI("HudGui")
    }

}