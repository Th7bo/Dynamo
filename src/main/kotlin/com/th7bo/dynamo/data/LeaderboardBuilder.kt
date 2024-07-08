package com.th7bo.dynamo.data

import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.parseDurationToSeconds
import org.bukkit.Location
import org.bukkit.entity.Player

class LeaderboardBuilder {
    var key: String? = null // added
    var names: MutableList<String> = mutableListOf() // added
    var location: Location? = null // added
    var refreshTime: String? = null // added
    var placeholders: MutableList<String> = mutableListOf() // added
    var dynamic = true // added
    var entries = 10 // added
    var formats: MutableMap<Int, String> = mutableMapOf() // added
    var defaultFormat: String? = null // added
    var topColor: String? = null // added
    var playerInTopLines: MutableList<String> = mutableListOf()
    var defaultLines: MutableList<String> = mutableListOf()

    var editing = false

    fun isValidConfiguration(p: Player): Boolean {
        if (key == null || names.isEmpty() || location == null || refreshTime == null || placeholders.isEmpty() || defaultFormat == null || topColor == null || playerInTopLines.isEmpty() || defaultLines.isEmpty()) {
            Misc.error(p, "Missing required fields")
            return false
        }
        return true
    }

    fun build(p: Player): Leaderboard? {
        if (isValidConfiguration(p).not()) {
            Misc.error(p, "Missing required fields")
            return null
        }
        if (!editing) {
            val lb = Leaderboard(key!!, true)
            lb.names = names
            lb.loc = location!!
            lb.refreshTime = refreshTime!!.parseDurationToSeconds()
            lb.placeholders = placeholders
            lb.entries = entries
            lb.formats = formats
            lb.defaultFormat = defaultFormat!!
            lb.topColor = topColor!!
            lb.playerInTopLines = playerInTopLines
            lb.defaultLines = defaultLines
            return lb
        } else {
            var lb = LeaderboardManager.leaderboards[key!!]
            lb!!.disable()
            lb.editing = false
            LeaderboardManager.leaderboards.remove(key!!)
            lb = Leaderboard(key!!, true)
            lb.names = names
            lb.loc = location!!
            lb.refreshTime = refreshTime!!.parseDurationToSeconds()
            lb.placeholders = placeholders
            lb.entries = entries
            lb.formats = formats
            lb.defaultFormat = defaultFormat!!
            lb.topColor = topColor!!
            lb.playerInTopLines = playerInTopLines
            lb.defaultLines = defaultLines
            lb.editing = false
            return lb
        }
    }
}