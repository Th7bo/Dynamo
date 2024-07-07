package com.th7bo.dynamo

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.th7bo.dynamo.listeners.EntityInteractListener
import com.th7bo.dynamo.listeners.ChatListener
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.Version
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.honkling.commando.spigot.SpigotCommandManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable


class Dynamo : JavaPlugin() {

    override fun onLoad() {
        super.onLoad()
        loadPacketEvents()
    }

    override fun onEnable() {
        // Plugin startup logic
        val version = Version(Bukkit.getMinecraftVersion())
        if (version.compareTo(1, 19, 4) < 0) {
            enabled = false
            Misc.error("This plugin is only compatible with 1.19.4 and above!")
            server.pluginManager.disablePlugin(this)
            return
        } else {
            Misc.log("Plugin is compatible with this version of Minecraft!")
        }
        instance = this
        enablePacketEvents()
        ChatListener()
        val commandManager = SpigotCommandManager(this)
        commandManager.registerCommands("com.th7bo.dynamo.commands")
        saveResource("leaderboards.yml", false)
        object : BukkitRunnable() {
            override fun run() {
                LeaderboardManager.init(instance)
            }
        }.runTaskLater(this, 40L)
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
    }

    private fun loadPacketEvents() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        //Are all listeners read only?
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
        PacketEvents.getAPI().load()
    }

    private fun enablePacketEvents() {
        PacketEvents.getAPI().eventManager.registerListener(
            EntityInteractListener(),
            PacketListenerPriority.HIGHEST
        )
        PacketEvents.getAPI().init()
    }

    companion object {
        lateinit var instance: Dynamo
            private set
        var enabled: Boolean = true
            private set
    }
}
