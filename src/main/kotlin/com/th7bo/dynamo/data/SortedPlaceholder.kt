package com.th7bo.dynamo.data

import com.th7bo.dynamo.Dynamo.Companion.instance
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.scheduler.BukkitRunnable

class SortedPlaceholder(private val placeholder: String) {
    private val unsortedMap = mutableMapOf<String, Double>()
    private val sortedMap = mutableMapOf<String, Double>()
    private var lastSorted = System.currentTimeMillis() - 2000

    init {
        println("PLACEHOLDER HAS BEEN CREATED")
        addOfflinePlayers()
        updatePlaceholderData()
        object : BukkitRunnable() {
            override fun run() {
                sortPlaceholder()
            }
        }.runTaskLater(instance, 40L)
    }

    fun addOfflinePlayers() {
        for (player in instance.server.offlinePlayers) { // offline players only need to be added once, since they don't change
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            val name = player.name
            if (name == null) {
                println("Player name for $player is null!")
                continue
            }
            if (unsortedMap[name] != null) continue
            unsortedMap[name] = value.toDoubleOrNull() ?: 0.0
            if (name.lowercase().contains("th7bo")) {
                println("$name has a value of ${unsortedMap[name]} in $placeholder!")
            }
        }
        for (player in instance.server.onlinePlayers) {
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            val name = player.name
            unsortedMap[name] = value.toDoubleOrNull() ?: 0.0
        }
    }

    fun updatePlaceholderData() {
        if (System.currentTimeMillis() - lastSorted < 1000) return
        for (player in instance.server.onlinePlayers) {
            val value = PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
            unsortedMap[player.name] = value.toDoubleOrNull() ?: continue
        }
    }

    fun sortPlaceholder() {
        if (System.currentTimeMillis() - lastSorted < 1000) return
        sortedMap.clear()
        unsortedMap.toList().sortedBy { (_, value) -> value }.reversed().toMap(sortedMap)
        lastSorted = System.currentTimeMillis()
    }

    fun getPlayer(index: Int): String? {
        return sortedMap.keys.elementAtOrNull(index)
    }

    fun getValue(index: Int): Double {
        return sortedMap.values.elementAtOrNull(index) ?: 0.0
    }

    fun getValue(name: String): Double {
        return sortedMap[name] ?: 0.0
    }

    fun getPlayerPosition(uuid: String) : Int {
        return sortedMap.keys.indexOf(uuid)
    }

    fun getSize(): Int {
        return sortedMap.size
    }

}