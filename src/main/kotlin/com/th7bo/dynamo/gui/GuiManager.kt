package com.th7bo.dynamo.gui

import com.th7bo.dynamo.data.LeaderboardBuilder
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.formatTime
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer

object GuiManager {
    var builders: MutableMap<UUID, LeaderboardBuilder> = mutableMapOf()
    var consumers: MutableMap<UUID, Consumer<String>> = mutableMapOf()
    fun openCreateGUI(p: Player, page: Int) {
        if (builders[p.uniqueId] == null) builders[p.uniqueId] = LeaderboardBuilder()
        LeaderboardsGUI.CreateGUI(p, page).open()
    }
    fun openFormatGUI(p: Player) = LeaderboardsGUI.FormatGUI(p).open()
    fun openPlaceholdersGUI(p: Player) = LeaderboardsGUI.PlaceholdersGUI(p).open()
    fun openNamesGUI(p: Player) = LeaderboardsGUI.NamesGUI(p).open()
    fun openTopLinesGUI(p: Player) = LeaderboardsGUI.PlayerInTopLinesGUI(p).open()
    fun openDefaultLinesGUI(p: Player) = LeaderboardsGUI.DefaultLinesGUI(p).open()

    fun openEditGUI(p: Player, key: String, page: Int) {
        if(!LeaderboardManager.leaderboards.containsKey(key)) {
            Misc.error(p, "Leaderboard not found")
            return
        }
        for (b in builders.values) if (b.key == key) {
            Misc.error(p, "Leaderboard already being edited")
            return
        }
        if (builders[p.uniqueId] == null) {
            builders[p.uniqueId] = LeaderboardBuilder()
            builders[p.uniqueId]!!.key = key
            builders[p.uniqueId]!!.editing = true

            val lb = LeaderboardManager.leaderboards[key]!!
            lb.editing = true
            builders[p.uniqueId]!!.names = lb.names
            builders[p.uniqueId]!!.location = lb.loc
            builders[p.uniqueId]!!.refreshTime = lb.refreshTime.formatTime()
            builders[p.uniqueId]!!.dynamic = true
            builders[p.uniqueId]!!.placeholders = lb.placeholders
            builders[p.uniqueId]!!.entries = lb.entries
            builders[p.uniqueId]!!.formats = lb.formats
            builders[p.uniqueId]!!.defaultFormat = lb.defaultFormat
            builders[p.uniqueId]!!.topColor = lb.topColor
            builders[p.uniqueId]!!.playerInTopLines = lb.playerInTopLines
            builders[p.uniqueId]!!.defaultLines = lb.defaultLines
        }

        LeaderboardsGUI.CreateGUI(p, page).open()
    }
}