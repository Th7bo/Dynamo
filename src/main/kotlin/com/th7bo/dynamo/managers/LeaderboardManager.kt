package com.th7bo.dynamo.managers

import com.th7bo.dynamo.data.Leaderboard
import com.th7bo.dynamo.data.SortedPlaceholder
import com.th7bo.dynamo.utils.Misc
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

object LeaderboardManager {

    lateinit var config: YamlConfiguration
    val leaderboards = mutableMapOf<String, Leaderboard>()
    var sortedPlaceholders: MutableMap<String, SortedPlaceholder> = mutableMapOf()
    private var id: Int = 10_000

    fun init(instance: Plugin) {
        config = YamlConfiguration.loadConfiguration(File(instance.dataFolder, "leaderboards.yml"))
        clear()
        if (config.getConfigurationSection("leaderboards") == null) return
        for (key in config.getConfigurationSection("leaderboards")!!.getKeys(false)) {
            try {
                config.getConfigurationSection("leaderboards.$key") ?: continue
                leaderboards[key] = Leaderboard(key).init()
            } catch (e: Exception) {
                Misc.handleError(e)
            }

        }
    }

    fun clear() {
        for (leaderboard in leaderboards.values) {
            leaderboard.disable()
            leaderboard.save()
        }
        leaderboards.clear()
        sortedPlaceholders.clear()
    }

    fun getID(): Int {
        id += 1
        return id
    }

}