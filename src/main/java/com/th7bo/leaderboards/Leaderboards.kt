package com.th7bo.leaderboards

import com.th7bo.leaderboards.listeners.debug.PacketData
import com.th7bo.leaderboards.listeners.debug.PacketListener
import com.th7bo.leaderboards.listeners.packets.EntityListener
import com.th7bo.leaderboards.managers.LeaderboardManager
import me.honkling.commando.spigot.SpigotCommandManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.slf4j.LoggerFactory
import java.io.File

class Leaderboards : JavaPlugin() {

    val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger("Leaderboards")

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        val commandManager = SpigotCommandManager(this)
        commandManager.registerCommands("com.th7bo.leaderboards.commands")
        PacketListener()
        EntityListener()
        saveDefaultConfig()
        config.options().copyDefaults(true)
        val configuration = YamlConfiguration.loadConfiguration(File(this.dataFolder, "config.yml"))
        PacketData.serverToClient = configuration.getBoolean("serverToClient", true)
        PacketData.clientToServer = configuration.getBoolean("clientToServer", true)
        PacketData.packetListenerEnabled = configuration.getBoolean("packetListenerEnabled", true)
        PacketData.ignoredPackets = configuration.getStringList("ignoredPackets").toMutableList()

        object : BukkitRunnable() {
            override fun run() {
                LeaderboardManager.init()
            }
        }.runTaskLater(this, 20L)
    }

    fun handleError(t: Throwable) {
        LOGGER.error("{}", t.javaClass.simpleName)
        LOGGER.error("-".repeat(30))
        LOGGER.error(t.message)
        for (element in t.stackTrace) LOGGER.error(" - {}", element.toString())
        LOGGER.error("-".repeat(30))

        if (t.cause != null) {
            LOGGER.error("Caused by:")
            handleError(t.cause!!)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        for (player in server.onlinePlayers) {
            PacketData.removeInjected(player)
        }
    }

    companion object {
        lateinit var instance: Leaderboards
            private set
    }
}
