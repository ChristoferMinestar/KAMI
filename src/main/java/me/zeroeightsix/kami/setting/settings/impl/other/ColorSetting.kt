package me.zeroeightsix.kami.setting.settings.impl.other

import me.zeroeightsix.kami.setting.settings.Setting
import me.zeroeightsix.kami.util.color.ColorHolder

class ColorSetting(
        name: String,
        value: ColorHolder,
        val hasAlpha: Boolean = true,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<ColorHolder>(name, value, visibility, { _, input -> if (!hasAlpha) input.apply { a = 255 } else input }, description)