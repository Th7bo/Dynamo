package com.th7bo.leaderboards.utils

import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

object Misc {

    fun getSpawnPacket(id: Int, loc: Location, type: EntityType<*>) : ClientboundAddEntityPacket {
        return ClientboundAddEntityPacket(id, UUID.randomUUID(), loc.x, loc.y, loc.z, loc.pitch, loc.yaw, type, 0, Vec3.ZERO, 0.0)
    }

    fun getDestroyPacket(id: Int) : ClientboundRemoveEntitiesPacket {
        return ClientboundRemoveEntitiesPacket(id)
    }

    fun getDestroyPacket(id: IntList) : ClientboundRemoveEntitiesPacket {
        return ClientboundRemoveEntitiesPacket(id)
    }

    fun sendPacket(p: Player, packet: Packet<*>) {
        (p as CraftPlayer).handle.connection.send(packet)
    }
}