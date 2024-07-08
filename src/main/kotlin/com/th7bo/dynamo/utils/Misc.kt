package com.th7bo.dynamo.utils

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.slf4j.LoggerFactory
import java.util.*

object Misc {

    private val OFFSETS: MutableMap<String, Int> = mutableMapOf(
        "1.19.4" to -1,
        "1.20" to -1,
        "1.20.1" to -1,
        "1.20.2" to 0,
        "1.20.4" to 0,
        "1.20.5" to 0,
        "1.20.6" to 0,
        "1.21" to 0
    )

    private val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger("Leaderboards")
    private val VERSION = Bukkit.getMinecraftVersion().replace("[.]", "")

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

    fun log(msg: String) {
        Bukkit.getConsoleSender().sendMessage(msg.parse(true))
    }

    fun error(msg: String) {
        Bukkit.getConsoleSender().sendMessage(("<dark_red><underlined>$msg").parse(true))
    }

    fun log(p: Player, msg: String) {
        p.sendMessage(msg.parse(true))
    }

    fun error(p: Player, msg: String) {
        p.sendMessage(("<dark_red><underlined>$msg").parse(true))
        p.playSound(p.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
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
    fun getDestroyPacket(vararg nums: Int) : Any {
        return WrapperPlayServerDestroyEntities(*nums)
    }



    fun sendPacket(p: Player, packet: PacketWrapper<*>) {
        PacketEvents.getAPI().playerManager.sendPacket(p, packet)
    }

    fun getDataPacket(id: Int, b: Byte, width: Float, height: Float, text: Component, color: Int, index: Int): PacketWrapper<*> {
        val offset = if (index != 0) index else OFFSETS.getOrDefault(VERSION, 0)
        val data = listOf(
            EntityData(15 + offset, EntityDataTypes.BYTE, b),
            EntityData(20 + offset, EntityDataTypes.FLOAT, width),
            EntityData(21 + offset, EntityDataTypes.FLOAT, height),
            EntityData(23 + offset, EntityDataTypes.ADV_COMPONENT, text),
            EntityData(25 + offset, EntityDataTypes.INT, color)
        )
        return WrapperPlayServerEntityMetadata(id, data)
    }

    fun getUpdatePacket(id: Int, text: Component): PacketWrapper<*> {
        val offset = OFFSETS.getOrDefault(VERSION, 0)
        val data = listOf(EntityData(23 + offset, EntityDataTypes.ADV_COMPONENT, text))
        return WrapperPlayServerEntityMetadata(id, data)
    }
}