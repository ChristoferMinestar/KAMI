package me.zeroeightsix.kami.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.InfoCalculator

object Ping : LabelHud(
    name = "Ping",
    category = Category.MISC,
    description = "Delay between client and server"
) {

    override fun SafeClientEvent.updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor)
        displayText.add("ms", secondaryColor)
    }

}