package me.zeroeightsix.kami.plugin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.net.URLClassLoader
import java.security.MessageDigest

internal class PluginLoader(
    val file: File
) {
    private val url = file.toURI().toURL()
    private val loader = URLClassLoader(arrayOf(url), this.javaClass.classLoader)
    val info: PluginInfo = loader.getResourceAsStream("plugin_info.json")?.let {
        PluginInfo.fromStream(it)
    } ?: throw FileNotFoundException("plugin_info.json not found in jar ${file.name}!")

    fun verify(): Boolean {
        val bytes = file.inputStream().use {
            it.readBytes()
        }

        val result = StringBuilder().run {
            sha256.digest(bytes).forEach {
                append(String.format("%02x", it))
            }

            toString()
        }

        KamiMod.LOG.info("SHA-256 checksum for ${file.name}: $result")

        return checksumSets.contains(result)
    }

    fun load(): Plugin {
        val clazz = Class.forName(info.mainClass, true, loader)
        val plugin = clazz.newInstance() as Plugin
        plugin.setInfo(info)
        return plugin
    }

    fun close() {
        loader.close()
    }

    private companion object {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        val type: Type = object : TypeToken<HashSet<String>>() {}.type
        val checksumSets = runCatching<HashSet<String>> {
            Gson().fromJson(File("verify.json").bufferedReader(), type)
        }.getOrElse { HashSet() }
    }
}