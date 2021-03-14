package org.kamiblue.client.command.commands

import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTException
import net.minecraft.nbt.NBTTagCompound
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.event.SafeExecuteEvent
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

object NBTCommand : ClientCommand(
    name = "nbt",
    description = "Get, copy, paste, clear NBT for item held in main hand"
) {

    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    init {
        literal("get") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                MessageSendHelper.sendChatMessage("NBT tags on item ${formatValue(itemStack.displayName)}")
                MessageSendHelper.sendRawChatMessage(nbtTag.toString())
            }
        }

        literal("copy") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                clipboard.setContents(StringSelection(nbtTag.toString()), null)

                MessageSendHelper.sendChatMessage("Copied NBT tags from item ${formatValue(itemStack.displayName)}")
            }
        }

        literal("paste") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe

                val nbtTag: NBTTagCompound

                try {
                    nbtTag = JsonToNBT.getTagFromJson(clipboard.getData(DataFlavor.stringFlavor) as String)
                } catch (e: NBTException) {
                    MessageSendHelper.sendErrorMessage("Invalid NBT data in clipboard")
                    return@executeSafe
                }

                itemStack.tagCompound = nbtTag
                MessageSendHelper.sendChatMessage("Pasted NBT tags to item ${formatValue(itemStack.displayName)}")
            }
        }

        literal("clear", "wipe") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                getNbtTag(itemStack) ?: return@executeSafe // Make sure it has a NBT tag before

                itemStack.tagCompound = NBTTagCompound()
            }
        }
    }

    private fun getNbtTag(itemStack: ItemStack): NBTTagCompound? {
        val nbtTag = itemStack.tagCompound

        if (nbtTag == null) {
            MessageSendHelper.sendChatMessage("Item ${formatValue(itemStack.displayName)} doesn't have NBT tag!")
        }

        return nbtTag
    }

    private fun SafeExecuteEvent.getHelpItemStack(): ItemStack? {
        val itemStack = player.inventory?.getCurrentItem()

        if (itemStack == null || itemStack.isEmpty) {
            MessageSendHelper.sendChatMessage("Not holding an item!")
            return null
        }

        return itemStack
    }

}