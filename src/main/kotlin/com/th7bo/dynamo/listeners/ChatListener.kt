package com.th7bo.dynamo.listeners

import com.th7bo.dynamo.Dynamo.Companion.instance
import com.th7bo.dynamo.gui.GuiManager
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent

class ChatListener : Listener{

    init {
        instance.server.pluginManager.registerEvents(this, instance)
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        if (GuiManager.consumers.containsKey(event.player.uniqueId)) {
            val consumer = GuiManager.consumers[event.player.uniqueId]!!
            consumer.accept(event.message)
            GuiManager.consumers.remove(event.player.uniqueId)
            event.isCancelled = true
        }
    }
}