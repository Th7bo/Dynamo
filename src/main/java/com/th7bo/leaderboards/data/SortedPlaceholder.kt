package com.th7bo.leaderboards.data

import com.th7bo.leaderboards.Leaderboards.Companion.instance
import me.clip.placeholderapi.PlaceholderAPI
import java.util.*

class SortedPlaceholder(val placeholder: String) {
    val unsortedMap = mutableMapOf<UUID, Double>()
    val sortedMap = mutableMapOf<UUID, Double>()

    init {
        updatePlaceholderData()
        sortPlaceholder()
    }

    fun updatePlaceholderData() {
        for (player in instance.server.offlinePlayers) {
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            unsortedMap[player.uniqueId] = value.toDoubleOrNull() ?: continue
        }
    }

    fun sortPlaceholder() {
        unsortedMap.toList().sortedBy { (_, value) -> value }.reversed().toMap(sortedMap)
    }

    fun getPlayer(index: Int): UUID? {
        return sortedMap.keys.elementAtOrNull(index)
    }

    fun getValue(index: Int): Double? {
        return sortedMap.values.elementAtOrNull(index) ?: 0.0
    }

    fun getPlayerPosition(uuid: UUID) : Int {
        return sortedMap.keys.indexOf(uuid)
    }

}