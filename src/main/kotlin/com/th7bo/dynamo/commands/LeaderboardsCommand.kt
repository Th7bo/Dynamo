@file:Command(
    "leaderboards",
    permission = "leaderboards.leaderboards"
)

package com.th7bo.dynamo.commands

import com.th7bo.dynamo.Dynamo
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun leaderboards(executor: CommandSender) {
    executor.sendMessage("<gray>Leaderboards:".parse(true))
    for (lb in LeaderboardManager.leaderboards.values) {
        executor.sendMessage("<gray> - <main>${lb.key}".parse())
    }
    executor.sendMessage("<gray>Dynamic Leaderboards:".parse(true))
    for (lb in LeaderboardManager.dynamicLeaderboards.values) {
        executor.sendMessage("<gray> - <main>${lb.key}".parse())
    }
}

fun reload(executor: Player) {
    LeaderboardManager.init(Dynamo.instance)
}