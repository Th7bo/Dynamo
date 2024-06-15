@file:Command(
    "debug",
    permission = "leaderboards.debug"
)

package com.th7bo.leaderboards.commands

import com.th7bo.leaderboards.listeners.debug.PacketData
import com.th7bo.leaderboards.listeners.debug.PacketData.updateConfig
import com.th7bo.leaderboards.managers.LeaderboardManager
import com.th7bo.leaderboards.utils.FormatHelper.Companion.parse
import com.th7bo.leaderboards.utils.toComponent
import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.time.measureTime

fun debug(executor: CommandSender) {
    executor.sendMessage("<gray>Client to server packets: ${if (PacketData.clientToServer) "<green>enabled" else "<red>disabled"}".parse(true))
    executor.sendMessage("<gray>Server to client packets: ${if (PacketData.serverToClient) "<green>enabled" else "<red>disabled"}".parse(true))
    executor.sendMessage("<gray>Packet listener: ${if (PacketData.packetListenerEnabled) "<green>enabled" else "<red>disabled"}".parse(true))
}

fun setClient(executor: CommandSender, enable: Boolean) {
    PacketData.clientToServer = enable
    executor.sendMessage("<gray>Client to server packets: ${if (enable) "<green>enabled" else "<red>disabled"}".parse(true))
    updateConfig()
}

fun setServer(executor: CommandSender, enable: Boolean) {
    PacketData.serverToClient = enable
    executor.sendMessage("<gray>Server to client packets: ${if (enable) "<green>enabled" else "<red>disabled"}".parse(true))
    updateConfig()
}

fun setListener(executor: CommandSender, enable: Boolean) {
    PacketData.setPacketListener(enable)
    executor.sendMessage("<gray>Packet listener: ${if (enable) "<green>enabled" else "<red>disabled"}".parse(true))
    updateConfig()
}

fun addIgnoredPacket(executor: CommandSender, packet: String) {
    for (split in packet.split(",")) {
        PacketData.ignoredPackets.add(split)
        executor.sendMessage("<gray>Added packet to ignored list: <main>$split".parse(true))
    }
    updateConfig()
}

fun removeIgnoredPacket(executor: CommandSender, packet: String) {
    PacketData.ignoredPackets.remove(packet)
    executor.sendMessage("<gray>Removed packet from ignored list: <main>$packet".parse(true))
    updateConfig()
}

fun lines(executor: CommandSender, key: String) {
    val player = executor as? Player ?: return
    val measure = measureTime {
        val lb = LeaderboardManager.dynamicLeaderboards[key] ?: return
        for (line in lb.getLines(player)) {
            player.sendMessage(line.toComponent())
        }
    }
    player.sendMessage("<gray>Time taken: <main>${measure}".parse(true))
}