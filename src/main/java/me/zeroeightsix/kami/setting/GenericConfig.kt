package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.config.AbstractConfig
import me.zeroeightsix.kami.setting.settings.AbstractSetting

internal object GenericConfig : AbstractConfig<Any>(
        "Generic",
        KamiMod.DIRECTORY
) {

    override fun <S : AbstractSetting<*>> Any.setting(setting: S): S {
        getGroupOrPut(this::class.simpleName!!).addSetting(setting)
        return setting
    }
}