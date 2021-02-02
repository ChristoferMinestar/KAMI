package me.zeroeightsix.kami.util

import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.events.ShutdownEvent
import me.zeroeightsix.kami.util.ConfigUtils.saveAll
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient

object Wrapper {
    @JvmStatic
    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()

    @JvmStatic
    val player: EntityPlayerSP?
        get() = minecraft.player

    @JvmStatic
    val world: WorldClient?
        get() = minecraft.world

    @JvmStatic
    fun saveAndShutdown() {
        if (!KamiMod.isReady()) return

        ShutdownEvent.post()

        println("Shutting down: saving KAMI configuration")
        saveAll()
        println("Configuration saved.")
    }
}