package com.th7bo.dynamo.listeners

import com.th7bo.dynamo.Dynamo.Companion.instance
import com.th7bo.dynamo.gui.GuiManager
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.Misc
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent

class EventListener : Listener{

    init {
        instance.server.pluginManager.registerEvents(this, instance)
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        if (GuiManager.consumers.containsKey(event.player.uniqueId)) {
            val consumer = GuiManager.consumers[event.player.uniqueId]!!
            event.isCancelled = true
            consumer.accept(event.message)
            return
        }
        val lb = LeaderboardManager.leaderboards["testing"] ?: return
        val placeholder = LeaderboardManager.sortedPlaceholders[lb.placeholders[0]] ?: return
        Misc.log(event.player, "Value: ${placeholder.getValue(event.player.name)}")

    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        if (GuiManager.builders.containsKey(event.player.uniqueId)) {
            val builder = GuiManager.builders[event.player.uniqueId]!!
            if (builder.editing) {
                val lb = LeaderboardManager.leaderboards[builder.key!!]!!
                lb.editing = false
            }
            GuiManager.builders.remove(event.player.uniqueId)
            GuiManager.consumers.remove(event.player.uniqueId)
        }
    }
}