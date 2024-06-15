package com.th7bo.leaderboards.listeners.debug

import com.th7bo.leaderboards.Leaderboards
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.io.File

object PacketData {

    fun updateConfig() {
        val configuration = YamlConfiguration.loadConfiguration(File(Leaderboards.instance.dataFolder, "config.yml"))
        configuration.set("serverToClient", serverToClient)
        configuration.set("clientToServer", clientToServer)
        configuration.set("packetListenerEnabled", packetListenerEnabled)
        configuration.set("ignoredPackets", ignoredPackets.toList())
        configuration.save(File(Leaderboards.instance.dataFolder, "config.yml"))
    }

    var clientToServer = true
    var serverToClient = true
    var packetListenerEnabled = true
    var ignoredPackets = mutableListOf<String>()

    fun setPacketListener(boolean: Boolean) {
        if (packetListenerEnabled == boolean) return
        packetListenerEnabled = boolean
        if (!boolean) {
            clientToServer = false
            serverToClient = false
        }
    }

    fun removeInjected(p: Player) {
        val channel = (p as CraftPlayer).handle.connection.connection.channel
        channel.eventLoop().submit<Any?> {
            channel.pipeline().remove("${p.name}-debug")
            null
        }
    }

    fun injectPlayer(p: Player) {
        removeInjected(p)
        val channelDuplexHandler: ChannelDuplexHandler = object : ChannelDuplexHandler() {
            @Throws(Exception::class)
            override fun channelRead(channelHandlerContext: ChannelHandlerContext, packet: Any) {
                if (clientToServer && !ignoredPackets.contains(packet::class.java.simpleName))
                    Leaderboards.instance.logger.info("C->S: ${packet::class.java.simpleName}")
                super.channelRead(channelHandlerContext, packet)
            }

            @Throws(Exception::class)
            override fun write(
                channelHandlerContext: ChannelHandlerContext,
                packet: Any,
                channelPromise: ChannelPromise
            ) {
                if (serverToClient && !ignoredPackets.contains(packet::class.java.simpleName))
                    Leaderboards.instance.logger.info("S->C: ${packet::class.java.simpleName}")
                super.write(channelHandlerContext, packet, channelPromise)
            }
        }
        val pipeline = (p as CraftPlayer).handle.connection.connection.channel.pipeline()
        pipeline.addBefore("packet_handler", "${p.name}-debug", channelDuplexHandler)
    }


}