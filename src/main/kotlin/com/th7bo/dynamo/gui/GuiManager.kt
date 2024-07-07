package com.th7bo.dynamo.gui

import com.th7bo.dynamo.data.LeaderboardBuilder
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
}