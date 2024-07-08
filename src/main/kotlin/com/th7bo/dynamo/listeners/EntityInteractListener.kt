package com.th7bo.dynamo.listeners

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.th7bo.dynamo.managers.LeaderboardManager
import org.bukkit.entity.Player
import java.util.*


class EntityInteractListener : PacketListener {

    private var lastRead: MutableMap<UUID, Long> = mutableMapOf()

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.player == null) return
        val player = event.player as Player
        val uuid = player.uniqueId
        if (event.packetType == PacketType.Play.Client.INTERACT_ENTITY && System.currentTimeMillis() - lastRead.getOrDefault(uuid, 0) > 50 ){
            lastRead[uuid] = System.currentTimeMillis()
            val wrappedEntity = WrapperPlayClientInteractEntity(event)
            val entityID = wrappedEntity.entityId
            for (lb in LeaderboardManager.leaderboards) {
                if (!lb.value.dynamic) continue
                if (lb.value.index[uuid] == null) lb.value.index[uuid] = 0
                if (lb.value.interactions.containsKey(uuid)) {
                    if (lb.value.interactions[uuid]!!.indexOf(entityID) == 0) {
                        var new = (lb.value.index[uuid] ?: 0) - 1
                        if (new < 0) new = lb.value.placeholders.size - 1
                        lb.value.index[uuid] = new
                        lb.value.update(player)
                    } else if (lb.value.interactions[uuid]!!.indexOf(entityID) == 1) {
                        var new = (lb.value.index[uuid] ?: 0) + 1
                        if (new > lb.value.placeholders.size - 1) new = 0
                        lb.value.index[uuid] = new
                        lb.value.update(player)
                    }
                }
            }
        }
    }

}