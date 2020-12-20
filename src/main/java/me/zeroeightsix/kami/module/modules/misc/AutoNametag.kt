package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemNameTag
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import org.kamiblue.event.listener.listener

@Module.Info(
    name = "AutoNametag",
    description = "Automatically nametags entities",
    category = Module.Category.MISC
)
object AutoNametag : Module() {
    private val modeSetting = setting("Mode", Mode.ANY)
    private val range = setting("Range", 3.5f, 2.0f..8.0f, 0.5f)
    private val debug = setting("Debug", false)

    private enum class Mode {
        WITHER, ANY
    }

    private var currentName = ""
    private var currentSlot = -1

    init {
        listener<SafeTickEvent> {
            findNameTags()
            useNameTag()
        }
    }

    private fun useNameTag() {
        val originalSlot = mc.player.inventory.currentItem
        for (entity in mc.world.loadedEntityList) {
            when (modeSetting.value) {
                Mode.WITHER -> if (entity is EntityWither && entity.getDisplayName().unformattedText != currentName) {
                    if (mc.player.getDistance(entity) <= range.value) {
                        if (debug.value) MessageSendHelper.sendChatMessage("Found unnamed " + entity.getDisplayName().unformattedText)
                        selectNameTags()
                        mc.playerController.interactWithEntity(mc.player, entity, EnumHand.MAIN_HAND)
                    }
                }
                Mode.ANY -> if ((entity is EntityMob || entity is EntityAnimal) && entity.displayName.unformattedText != currentName) {
                    if (mc.player.getDistance(entity) <= range.value) {
                        if (debug.value) MessageSendHelper.sendChatMessage("Found unnamed " + entity.displayName.unformattedText)
                        selectNameTags()
                        mc.playerController.interactWithEntity(mc.player, entity, EnumHand.MAIN_HAND)
                    }
                }
            }
        }
        mc.player.inventory.currentItem = originalSlot
    }

    private fun selectNameTags() {
        if (currentSlot == -1 || !isNametag(currentSlot)) {
            MessageSendHelper.sendErrorMessage("$chatName Error: No nametags in hotbar")
            disable()
            return
        }
        mc.player.inventory.currentItem = currentSlot
    }

    private fun findNameTags() {
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item is ItemBlock) continue

            if (isNametag(i)) {
                currentName = stack.displayName
                currentSlot = i
            }
        }
    }

    /* In case they run out of nametags, check again */
    private fun isNametag(i: Int): Boolean {
        val stack = mc.player.inventory.getStackInSlot(i)
        val tag = stack.item
        return tag is ItemNameTag && stack.displayName != "Name Tag"
    }
}