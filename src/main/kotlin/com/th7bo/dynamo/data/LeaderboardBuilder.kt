package com.th7bo.dynamo.data

import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.parseDurationToSeconds
import org.bukkit.Location
import org.bukkit.entity.Player

class LeaderboardBuilder {
    var key: String? = null
    var names: List<String> = listOf()
    var location: Location? = null
    var refreshTime: String? = null
    var placeholders: List<String> = listOf()
    var dynamic = true
    var entries = 10
    var formats: MutableMap<Int, String> = mutableMapOf()
    var defaultFormat: String? = null
    var topColor: String? = null
    var playerInTopLines: MutableList<String> = mutableListOf()
    var defaultLines: MutableList<String> = mutableListOf()

    fun buildDynamic(p: Player): DynamicLeaderboard? {
        if (isValidConfiguration(p).not()) {
            Misc.error(p, "Missing required fields")
            return null
        }
        val lb = DynamicLeaderboard(key!!)
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
    }

    fun buildNormal(p: Player): NormalLeaderboards? {
        if (isValidConfiguration(p).not()) {
            Misc.error(p, "Missing required fields")
            return null
        }
        return NormalLeaderboards(key!!)
    }

    private fun isValidConfiguration(p: Player): Boolean {
        if (key == null || names.isEmpty() || location == null || refreshTime == null || placeholders.isEmpty() || formats.isEmpty() || defaultFormat == null || topColor == null || playerInTopLines.isEmpty() || defaultLines.isEmpty()) {
            Misc.error(p, "Missing required fields")
            return false
        }
        return true
    }
}