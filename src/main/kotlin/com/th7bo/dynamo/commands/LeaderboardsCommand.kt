package com.th7bo.dynamo.commands

import com.th7bo.dynamo.gui.GuiManager
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class LeaderboardsCommand : CommandExecutor, TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        if(!sender.isOp) {
            return mutableListOf()
        }
        if(args?.size == 1) {
            return mutableListOf("edit", "create")
        } else if(args?.size == 2) {
            var list = LeaderboardManager.leaderboards.keys.toMutableList()
            list = (list + LeaderboardManager.leaderboards.keys).toMutableList()
            val copy = list.toMutableList()
            for(i in copy) {
                if(!i.startsWith(args[1])) {
                    list.remove(i)
                }
            }
            return list
        }
        return mutableListOf()
    }

    override fun onCommand(executor: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (executor !is Player) return true
        if (!executor.hasPermission("dynamo.leaderboards")) {
            executor.sendMessage("<red>You do not have permission to use this command.".parse())
            return true
        }
        if (args.isEmpty()) {
            executor.sendMessage("<gray>Leaderboards:".parse(true))
            for (lb in LeaderboardManager.leaderboards.values) {
                executor.sendMessage("<gray> - <main>${lb.key}".parse())
            }
            return true
        }
        if (args[0] == "edit") {
            if (args.size < 2) {
                executor.sendMessage("<red>Usage: /leaderboards edit <key>".parse())
                return true
            }
            GuiManager.openEditGUI(executor, args[1], 1)
            return true
        }
        if (args[0] == "create") {
            GuiManager.openCreateGUI(executor, 1)
            return true
        }
        return true
    }
}


