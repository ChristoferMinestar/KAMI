package me.zeroeightsix.kami.gui.mc

import com.google.gson.JsonParser
import me.zeroeightsix.installer.Installer
import me.zeroeightsix.kami.NecronClient
import me.zeroeightsix.kami.util.WebUtils
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.filesystem.FolderHelper
import me.zeroeightsix.kami.util.filesystem.FolderHelper.getModsFolder
import me.zeroeightsix.kami.util.filesystem.OperatingSystemHelper
import me.zeroeightsix.kami.util.filesystem.OperatingSystemHelper.getOS
import net.minecraft.client.gui.*
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.FMLCommonHandler
import org.codehaus.plexus.util.Os
import org.kamiblue.commons.utils.ConnectionUtils
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.system.exitProcess
import java.lang.Compiler.command
import java.util.ArrayList


class KamiGuiUpdateNotification(private val buttonId: Int) : GuiScreen() {

    private val message = "A newer release of NECRON Client is available ($latest)."

    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, width / 2 - 100, 200, "Download Latest (Recommended)"))
        buttonList.add(GuiButton(1, width / 2 - 100, 230, "${TextFormatting.RED}Use Current Version"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, title, width / 2, 80, ColorConverter.rgbToHex(155, 144, 255))
        drawCenteredString(fontRenderer, message, width / 2, 110, ColorConverter.rgbToHex(255, 255, 255))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
    fun getNecronJars(): ArrayList<File>? {
        val mods = File(getModsFolder(getOS()))
        val files = mods.listFiles()
        val foundFiles = ArrayList<File>()
        var found = false
        for (file in files) {
            val match = file.name.matches(Regex(".*[Nn][Ee][Cc][Rr][Oo][Nn].*"))
            if (match) {
                foundFiles.add(file)
                found = true
            }
        }
        return if (found) foundFiles else null
    }
    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            val modsFolder = getModsFolder(getOS())
            val necronJars = getNecronJars()
            val fileExtension = if (getOS().name == "WINDOWS") ".bat" else ".sh"

            if (necronJars != null) {
                val file = File("$modsFolder${File.separator}Necron-Updater$fileExtension")
                file.writeText("java -jar ${necronJars[0]}")
                NecronClient.LOG.info("Starting Updater with command: $modsFolder${File.separator}Necron-Updater$fileExtension")
                ProcessBuilder("$modsFolder${File.separator}Necron-Updater$fileExtension").start()
                FMLCommonHandler.instance().exitJava(1, true)
            }
        }

        val screen = if (buttonId == 1) GuiWorldSelection(GuiMainMenu()) // Single
        else GuiMultiplayer(GuiMainMenu()) // Multi

        mc.displayGuiScreen(screen)
    }

    companion object {
        private const val title = "NECRON Client Update"

        var latest: String? = null // latest version (null if no internet or exception occurred)
        var isLatest = false

        @JvmStatic
        fun updateCheck() {
            try {
                NecronClient.LOG.info("Attempting NECRON Client update check...")

                val parser = JsonParser()
                val rawJson = ConnectionUtils.requestRawJsonFrom(NecronClient.DOWNLOADS_API) {
                    throw it
                }

                latest = parser.parse(rawJson).asJsonObject.getAsJsonObject("stable")["name"].asString
                isLatest = latest.equals(NecronClient.VERSION_MAJOR)

                if (!isLatest) {
                    NecronClient.LOG.warn("You are running an outdated version of NECRON CLient.\nCurrent: ${NecronClient.VERSION_MAJOR}\nLatest: $latest")
                } else {
                    NecronClient.LOG.info("Your NECRON Client (" + NecronClient.VERSION_MAJOR + ") is up-to-date with the latest stable release.")
                }
            } catch (e: IOException) {
                NecronClient.LOG.error("Oes noes! An exception was thrown during the update check.", e)
            }
        }
    }
}