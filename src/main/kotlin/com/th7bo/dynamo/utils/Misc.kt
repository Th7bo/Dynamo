package com.th7bo.dynamo.utils

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.slf4j.LoggerFactory
import java.util.*

object Misc {

    val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger("Leaderboards")

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

    //
    fun getSpawnPacket(id: Int, loc: Location): PacketWrapper<*> {
        return WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.TEXT_DISPLAY, loc.toPacketEvents(), 0f, 0, null)
    }

    fun getSpawnPacketInteraction(id: Int, loc: Location) : PacketWrapper<*> {
        return WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.INTERACTION, loc.toPacketEvents(), 0f, 0, null)
    }

    fun getDestroyPacket(id: Int) : PacketWrapper<*> {
        return WrapperPlayServerDestroyEntities(id)
    }
//    fun getDestroyPacket(vararg nums: Int) : Any {
//        return WrapperPlayServerDestroyEntities(nums)
//    }



    fun sendPacket(p: Player, packet: PacketWrapper<*>) {
//        println("Sending packet to ${p.name}")
//        println("Packet: ${packet::class.simpleName}")
        PacketEvents.getAPI().playerManager.sendPacket(p, packet)
    }

    fun getDataPacket(id: Int, b: Byte, width: Float, height: Float, text: Component, color: Int): PacketWrapper<*> {
        val data = listOf(EntityData(15, EntityDataTypes.BYTE, b), EntityData(20, EntityDataTypes.FLOAT, width), EntityData(21, EntityDataTypes.FLOAT, height), EntityData(23, EntityDataTypes.ADV_COMPONENT, text), EntityData(25, EntityDataTypes.INT, color))
        val packet = WrapperPlayServerEntityMetadata(id, data)

//        val dataList: ArrayList<SynchedEntityData.DataValue<*>> = ArrayList()
//        dataList.add(SynchedEntityData.DataValue(15, EntityDataSerializers.BYTE, b)) // Fixed
//        dataList.add(SynchedEntityData.DataValue(20, EntityDataSerializers.FLOAT, width)) // Width
//        dataList.add(SynchedEntityData.DataValue(21, EntityDataSerializers.FLOAT, height)) // Height
//        dataList.add(SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.WRAPPER_AWARE_SERIALIZER.serialize(text))) // Text
//        dataList.add(SynchedEntityData.DataValue(25, EntityDataSerializers.INT, color)) // Background
        return packet
    }

    fun getUpdatePacket(id: Int, text: Component): PacketWrapper<*> {

        val data = listOf(EntityData(23, EntityDataTypes.ADV_COMPONENT, text))
        val packet = WrapperPlayServerEntityMetadata(id, data)

//        val dataList = ArrayList<SynchedEntityData.DataValue<*>>()
//        dataList.add(SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.WRAPPER_AWARE_SERIALIZER.serialize(text)))
        return packet
    }
}