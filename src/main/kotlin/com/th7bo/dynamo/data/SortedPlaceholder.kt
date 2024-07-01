package com.th7bo.dynamo.data

import com.th7bo.dynamo.Dynamo.Companion.instance
import me.clip.placeholderapi.PlaceholderAPI
import java.util.*

class SortedPlaceholder(val placeholder: String) {
    val unsortedMap = mutableMapOf<UUID, Double>()
    val sortedMap = mutableMapOf<UUID, Double>()
    var lastSorted = System.currentTimeMillis()

    init {
        addOfflinePlayers()
        updatePlaceholderData()
        sortPlaceholder()
    }

    fun addOfflinePlayers() {
        for (player in instance.server.offlinePlayers) { // offline players only need to be added once, since they don't change
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            unsortedMap[player.uniqueId] = value.toDoubleOrNull() ?: continue
        }
    }

    fun updatePlaceholderData() {
        if (System.currentTimeMillis() - lastSorted < 1000) return
        for (player in instance.server.onlinePlayers) {
            println("Updating placeholder data for $placeholder for ${player.name}")
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            unsortedMap[player.uniqueId] = value.toDoubleOrNull() ?: continue
        }
        println("Updated placeholder data for $placeholder")
        println("Unsorted map size: ${unsortedMap.size}")
    }

    fun sortPlaceholder() {
        if (System.currentTimeMillis() - lastSorted < 1000) return
        sortedMap.clear()
        unsortedMap.toList().sortedBy { (_, value) -> value }.reversed().toMap(sortedMap)
        lastSorted = System.currentTimeMillis()
    }

    fun getPlayer(index: Int): UUID? {
        return sortedMap.keys.elementAtOrNull(index)
    }

    fun getValue(index: Int): Double {
        return sortedMap.values.elementAtOrNull(index) ?: 0.0
    }

    fun getPlayerPosition(uuid: UUID) : Int {
        return sortedMap.keys.indexOf(uuid)
    }

    fun getSize(): Int {
        return sortedMap.size
    }

}