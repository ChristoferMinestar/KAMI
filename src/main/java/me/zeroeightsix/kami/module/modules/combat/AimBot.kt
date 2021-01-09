package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntityClosest
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
internal object AimBot : Module(
    name = "AimBot",
    description = "Automatically aims at entities for you.",
    category = Category.COMBAT,
    modulePriority = 20
) {
    private val bowOnly = setting("BowOnly", true)
    private val autoSwap = setting("AutoSwap", false, { bowOnly.value })

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (bowOnly.value && player.heldItemMainhand.item != Items.BOW) {
                if (autoSwap.value) InventoryUtils.swapSlotToItem(261)
                return@safeListener
            }
            CombatManager.target?.let {
                faceEntityClosest(it)
            }
        }
    }
}