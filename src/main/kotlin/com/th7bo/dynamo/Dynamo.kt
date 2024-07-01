package com.th7bo.dynamo

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.th7bo.dynamo.listeners.EntityInteractListener
import com.th7bo.dynamo.managers.LeaderboardManager
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.honkling.commando.spigot.SpigotCommandManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable


class Dynamo : JavaPlugin() {

    override fun onLoad() {
        super.onLoad()
        loadPacketEvents()
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        enablePacketEvents()
        val commandManager = SpigotCommandManager(this)
        commandManager.registerCommands("com.th7bo.leaderboards.commands")
        saveResource("leaderboards.yml", false)

        object : BukkitRunnable() {
            override fun run() {
                LeaderboardManager.init(instance)
            }
        }.runTaskLater(this, 40L)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        PacketEvents.getAPI().terminate()
    }

    fun loadPacketEvents() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        //Are all listeners read only?
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
        PacketEvents.getAPI().load()
    }

    fun enablePacketEvents() {
        PacketEvents.getAPI().eventManager.registerListener(
            EntityInteractListener(),
            PacketListenerPriority.LOW
        )
        PacketEvents.getAPI().init()
    }

    companion object {
        lateinit var instance: Dynamo
            private set
    }
}
