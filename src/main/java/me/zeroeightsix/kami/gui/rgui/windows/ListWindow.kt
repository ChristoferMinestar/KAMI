package me.zeroeightsix.kami.gui.rgui.windows

import com.google.gson.annotations.Expose
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

open class ListWindow(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float,
        vararg childrenIn: Component
) : TitledWindow(name, posX, posY, width, height) {
    @Expose val children = LinkedList<Component>()

    override val minWidth = 50.0f
    override val minHeight = 200.0f
    override val maxWidth = 200.0f
    override val maxHeight get() = mc.displayHeight.toFloat()
    override val resizable: Boolean get() = hoveredChild == null

    protected val lineSpace = 2.0f
    private var hoveredChild: Component? = null
        set(value) {
            if (value == field) return
            (field as? InteractiveComponent)?.onLeave(KamiClickGui.getRealMousePos())
            (value as? InteractiveComponent)?.onHover(KamiClickGui.getRealMousePos())
            field = value
        }

    private val scrollTimer = TimerUtils.TickTimer()
    private var scrollSpeed = 0.0f

    private var scrollProgress = 0.0f
        set(value) {
            prevScrollProgress = field
            field = value
        }
    private var prevScrollProgress = 0.0f
    protected val renderScrollProgress
        get() = prevScrollProgress + (scrollProgress - prevScrollProgress) * mc.renderPartialTicks

    init {
        children.addAll(childrenIn)
        updateChild()
    }

    private fun updateChild() {
        var y = (if (draggableHeight != height) draggableHeight else 0.0f) + lineSpace
        for (child in children) {
            if (!child.visible) continue
            child.posX = lineSpace
            child.posY = y
            child.width = width - lineSpace * 2.0f
            y += child.height + lineSpace
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        for (child in children) child.onGuiInit()
        updateChild()
    }

    override fun onResize() {
        super.onResize()
        updateChild()
    }

    override fun onTick() {
        super.onTick()
        if (children.isEmpty()) return
        val lastVisible = children.lastOrNull { it.visible }
        val maxScrollProgress = lastVisible?.let { max(it.posY + it.height + lineSpace - height, 0.01f) }?: draggableHeight

        scrollProgress = (scrollProgress + scrollSpeed)
        scrollSpeed *= 0.5f
        if (scrollTimer.tick(100L, false)) {
            if (scrollProgress < 0.0) {
                scrollSpeed = scrollProgress * -0.25f
            } else if (scrollProgress > maxScrollProgress) {
                scrollSpeed = (scrollProgress - maxScrollProgress) * -0.25f
            }
        }

        updateChild()
        for (child in children) child.onTick()
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        GlStateUtils.scissor(
                ((renderPosX + lineSpace) * ClickGUI.getScaleFactor()).roundToInt(),
                ((mc.displayHeight - (renderPosY + renderHeight) * ClickGUI.getScaleFactor())).roundToInt(),
                ((renderWidth - lineSpace * 2.0) * ClickGUI.getScaleFactor()).roundToInt(),
                ((renderHeight - draggableHeight) * ClickGUI.getScaleFactor()).roundToInt()
        )
        glEnable(GL_SCISSOR_TEST)
        glTranslatef(0.0f, -renderScrollProgress, 0.0f)
        for (child in children) {
            if (!child.visible) continue
            glPushMatrix()
            glTranslatef(child.posX, child.posY, 0.0f)
            child.onRender(vertexHelper, absolutePos.add(child.posX, child.posY))
            glPopMatrix()
        }
        glDisable(GL_SCISSOR_TEST)
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)
        val relativeMousePos = mousePos.subtract(posX, posY - renderScrollProgress)
        if (Mouse.getEventDWheel() != 0) {
            scrollTimer.reset()
            scrollSpeed -= Mouse.getEventDWheel() * 0.1f
            updateHovered(relativeMousePos)
        }
        if (mouseState != MouseState.DRAG) {
            updateHovered(relativeMousePos)
        }
    }

    private fun updateHovered(relativeMousePos: Vec2f) {
        hoveredChild = if (relativeMousePos.y < draggableHeight || relativeMousePos.x < lineSpace || relativeMousePos.x > renderWidth - lineSpace) null
        else children.firstOrNull { it.visible && relativeMousePos.y in it.posY..it.posY + it.height }
    }

    override fun onLeave(mousePos: Vec2f) {
        super.onLeave(mousePos)
        hoveredChild = null
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onClick(relativePos, buttonId)
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onRelease(relativePos, buttonId)
        }
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onDrag(relativePos, clickPos, buttonId)
        }
    }
}