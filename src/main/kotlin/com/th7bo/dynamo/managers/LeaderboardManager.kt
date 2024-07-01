package com.th7bo.dynamo.managers

import com.th7bo.dynamo.data.DynamicLeaderboard
import com.th7bo.dynamo.data.NormalLeaderboards
import com.th7bo.dynamo.data.SortedPlaceholder
import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.asLocation
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

object LeaderboardManager {

    lateinit var config: YamlConfiguration
    val leaderboards = mutableMapOf<String, NormalLeaderboards>()
    val dynamicLeaderboards = mutableMapOf<String, DynamicLeaderboard>()
    var sortedPlaceholders: MutableMap<String, SortedPlaceholder> = mutableMapOf()
    private var id: Int = 10_000

    fun init(instance: Plugin) {
        config = YamlConfiguration.loadConfiguration(File(instance.dataFolder, "leaderboards.yml"))
        clear()
        for (key in config.getConfigurationSection("leaderboards")!!.getKeys(false)) {
            try {
                val leaderboard = config.getConfigurationSection("leaderboards.$key") ?: continue
                val loc = leaderboard.getString("location")!!.asLocation()
                val dynamic = leaderboard.getBoolean("dynamic", false)
                if (dynamic) {
                    val variable = leaderboard.getStringList("placeholders")
                    dynamicLeaderboards[key] = DynamicLeaderboard(instance, key, loc, variable)
                } else {
                    val variable = leaderboard.getString("placeholders")!!
                    leaderboards[key] = NormalLeaderboards(key, loc, variable)
                }
            } catch (e: Exception) {
                Misc.handleError(e)
            }

        }
    }

    fun clear() {
        for (leaderboard in leaderboards.values) {
//            leaderboard.disable()
        }
        for (leaderboard in dynamicLeaderboards.values) {
            leaderboard.disable()
        }
        sortedPlaceholders.forEach() { (_, sorted) ->
            run {
                sorted.updatePlaceholderData()
                sorted.sortPlaceholder()
            }
        }
        leaderboards.clear()
        dynamicLeaderboards.clear()
    }

    fun getID(): Int {
        id += 1
        return id
    }

}