@file:Command(
    "test",
    permission = "leaderboards.test"
)

package com.th7bo.dynamo.commands

import com.th7bo.dynamo.gui.GuiManager
import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender

fun test(executor: CommandSender) {
    val player = executor as? org.bukkit.entity.Player ?: return
    GuiManager.openCreateGUI(player, 1)
}