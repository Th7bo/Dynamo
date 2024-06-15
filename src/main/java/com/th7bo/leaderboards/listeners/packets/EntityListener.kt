package com.th7bo.leaderboards.listeners.packets

import com.th7bo.leaderboards.Leaderboards
import com.th7bo.leaderboards.managers.LeaderboardManager
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EntityListener : Listener {

    @EventHandler
    fun quit(e: PlayerQuitEvent) {
        removeInjected(e.player)
    }

    @EventHandler
    fun join(e: PlayerJoinEvent) {
        injectPlayer(e.player)
    }

    init {
        Leaderboards.instance.server.pluginManager.registerEvents(this, Leaderboards.instance)
        for (player in Leaderboards.instance.server.onlinePlayers) {
            injectPlayer(player)
        }
    }

    fun injectPlayer(p: Player) {
        var lastRead = System.currentTimeMillis()
        val channelDuplexHandler: ChannelDuplexHandler = object : ChannelDuplexHandler() {
            @Throws(Exception::class)
            override fun channelRead(channelHandlerContext: ChannelHandlerContext, packet: Any) {
                if (packet is ServerboundInteractPacket && System.currentTimeMillis() - lastRead > 50) {
                    lastRead = System.currentTimeMillis()
                    val id = packet.entityId
                    for (lb in LeaderboardManager.dynamicLeaderboards) {
                        if (lb.value.index[p.uniqueId] == null) lb.value.index[p.uniqueId] = 0
                        if (lb.value.interactions.containsKey(p.uniqueId)) {
                            if (lb.value.interactions[p.uniqueId]!!.indexOf(id) == 0) {
                                var new = (lb.value.index[p.uniqueId] ?: 0) - 1
                                if (new < 0) new = lb.value.placeholders.size - 1
                                lb.value.index[p.uniqueId] = new
                                lb.value.update(p, true)
                            } else if (lb.value.interactions[p.uniqueId]!!.indexOf(id) == 1) {
                                var new = (lb.value.index[p.uniqueId] ?: 0) + 1
                                if (new > lb.value.placeholders.size - 1) new = 0
                                lb.value.index[p.uniqueId] = new
                                lb.value.update(p, true)
                            }
                        }

                    }
                }
                super.channelRead(channelHandlerContext, packet)
            }
        }
        val pipeline = (p as CraftPlayer).handle.connection.connection.channel.pipeline()
        pipeline.addBefore("packet_handler", "${p.name}-leaderboards", channelDuplexHandler)
    }

    fun removeInjected(p: Player) {
        val channel = (p as CraftPlayer).handle.connection.connection.channel
        channel.eventLoop().submit<Any?> {
            channel.pipeline().remove("${p.name}-leaderboards")
            null
        }
    }

}