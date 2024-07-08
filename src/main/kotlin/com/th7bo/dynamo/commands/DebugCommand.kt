package com.th7bo.dynamo.commands

import com.th7bo.dynamo.gui.GuiManager
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DebugCommand : CommandExecutor {
    override fun onCommand(executor: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        if (executor !is Player) return true
        if (!executor.hasPermission("dynamo.leaderboards")) {
            executor.sendMessage("<red>You do not have permission to use this command.".parse())
            return true
        }
        GuiManager.openCreateGUI(executor, 1)
        return true
    }
}