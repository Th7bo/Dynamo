@file:Command(
    "debug",
    permission = "leaderboards.debug"
)

package com.th7bo.dynamo.commands

import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.toComponent
import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender

fun debug(executor: CommandSender, key: String) {
    val player = executor as? org.bukkit.entity.Player ?: return
    val lb = LeaderboardManager.dynamicLeaderboards[key] ?: return
    for (line in lb.getLines(player)) {
        executor.sendMessage(line.toComponent())
    }
    executor.sendMessage("Disabled")
}