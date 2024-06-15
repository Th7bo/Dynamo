package com.th7bo.leaderboards.listeners.debug

import com.th7bo.leaderboards.Leaderboards
import com.th7bo.leaderboards.managers.LeaderboardManager
import com.th7bo.leaderboards.utils.toComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PacketListener  : Listener {

    @EventHandler
    fun removePlayer(e: PlayerQuitEvent) {
        PacketData.removeInjected(e.player)
    }

    @EventHandler
    fun injectPlayer(e: PlayerJoinEvent) {
        PacketData.injectPlayer(e.player)
        val lb = LeaderboardManager.dynamicLeaderboards["test"] ?: return
        for (line in lb.getLines(e.player))
            e.player.sendMessage(line.toComponent())
    }

    init {
        Leaderboards.instance.server.pluginManager.registerEvents(this, Leaderboards.instance)
        for (player in Leaderboards.instance.server.onlinePlayers) {
            PacketData.injectPlayer(player)
        }
    }
}