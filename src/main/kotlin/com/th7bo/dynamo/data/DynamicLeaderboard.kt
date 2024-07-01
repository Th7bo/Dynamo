package com.th7bo.dynamo.data

import com.th7bo.dynamo.managers.LeaderboardManager.sortedPlaceholders
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import org.bukkit.Color
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.time.measureTime


class DynamicLeaderboard(plugin: Plugin, var key: String, var loc: Location, var placeholders: List<String>) : Listener{

    var interactions: MutableMap<UUID, ArrayList<Int>> = mutableMapOf()
    var index: MutableMap<UUID, Int> = mutableMapOf()
    private var section = LeaderboardManager.config.getConfigurationSection("leaderboards.$key")
    private var textDisplayID: MutableMap<UUID, Int> = mutableMapOf()
    private var name = section!!.getString("name")!!
    private var refreshTime = (section!!.getString("refresh-time")!!).parseDurationToSeconds()
    private var names = section!!.getStringList("names")
    private var lastReset = System.currentTimeMillis()
//    private var sortedPlaceholders: MutableList<SortedPlaceholder> = mutableListOf()
    private var taskID: MutableList<Int> = mutableListOf()

    private lateinit var leftVector: Vector
    private lateinit var rightVector: Vector

    init {
        if (section == null) {
            LeaderboardManager.dynamicLeaderboards.remove(key)
        } else {
            val loca = loc.clone()
            val left = loca.clone().getCustomRelative(0.0, -1.5, 0.0)
            val right = loca.clone().getCustomRelative(0.0, 1.5, 0.0)
            section = section!!
            leftVector = Vector(left.x - loca.x, left.y - loca.y, left.z - loca.z)
            rightVector = Vector(right.x - loca.x, right.y - loca.y, right.z - loca.z)

            plugin.server.pluginManager.registerEvents(this, plugin)
            for (holder in placeholders) {
                if (!sortedPlaceholders.containsKey(holder)) {
                    sortedPlaceholders[holder] = SortedPlaceholder(holder)
                }
            }
            taskID.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
                updatePlaceholders()
                update()
            }, 0, 20L * refreshTime))
            taskID.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
                update()
            }, 0, 10L))
        }

    }

    private fun updatePlaceholders() {
        lastReset = System.currentTimeMillis()
        val time = measureTime {
            for (holder in placeholders) {
                if (sortedPlaceholders.containsKey(holder)) {
                    sortedPlaceholders[holder]!!.updatePlaceholderData()
                    sortedPlaceholders[holder]!!.sortPlaceholder()
                }
            }
        }
        Bukkit.broadcast("Updated $key in $time".parse(true))
    }

    private fun getTimeLeft(): String {
        val time = ((lastReset + refreshTime * 1000) - System.currentTimeMillis()) / 1000
        if (time <= 0) {
            lastReset = System.currentTimeMillis()
            return "Now"
        }
        return time.formatTime()
    }

    @EventHandler
    fun join(e: PlayerJoinEvent) {
        if (e.player.world != loc.world) return
        update(e.player, true)
    }

    @EventHandler
    fun respawn(e: PlayerRespawnEvent) {
        if (e.player.world != loc.world) return despawnAll(e.player)
        update(e.player, true)
    }

    @EventHandler
    fun quit(e: PlayerQuitEvent) {
        despawnAll(e.player)
    }

    @EventHandler
    fun teleport(e: PlayerTeleportEvent) {
        if (e.to.world != loc.world) return despawnAll(e.player)
        update(e.player, true)
    }

    private fun despawnAll(p: Player) {
        if (interactions[p.uniqueId] == null) return
        if (textDisplayID[p.uniqueId] == null) return
        for (id in interactions[p.uniqueId]!!) {
            val packet = Misc.getDestroyPacket(id)
            Misc.sendPacket(p, packet)
        }

        interactions[p.uniqueId]!!.clear()
        textDisplayID.remove(p.uniqueId)
    }

    private fun update() {
        for (p in Bukkit.getOnlinePlayers()) {
            update(p)
        }
    }



    fun update(p: Player, updateInteractions: Boolean = false) {
        if (p.world != loc.world) return despawnAll(p)
        if (p.location.distanceSquared(loc) >= 300) return despawnAll(p)
        if (interactions[p.uniqueId] == null) interactions[p.uniqueId] = arrayListOf()
        if (updateInteractions) {
            if (interactions[p.uniqueId] != null) {
                for (id in interactions[p.uniqueId]!!) {
                    val packet = Misc.getDestroyPacket(id)
                    Misc.sendPacket(p, packet)
                }
                interactions[p.uniqueId]!!.clear()
            }
        }


        val lines = getLines(p)
        var maxWidth = 0
        for (line in lines) {
            val parsed = line.toComponent()
            val width = Font.getWidth(PlainTextComponentSerializer.plainText().serialize(parsed))
            if (width > maxWidth) {
                maxWidth = width
            }
        }
        val comp = lines.joinToString("\n").toComponent()

        val locationLeft = loc.clone().add(leftVector.clone().multiply(maxWidth / 100))
        val locationRight = loc.clone().add(rightVector.clone().multiply(maxWidth / 100))
        spawnInteractions(p, locationLeft, locationRight)

        if (textDisplayID[p.uniqueId] == null) {
            println("Spawning display")
            val id = LeaderboardManager.getID()
            val spawnPacket = Misc.getSpawnPacket(id, loc)
            Misc.sendPacket(p, spawnPacket)

            val dataPacket = Misc.getDataPacket(id, 0.toByte(), 100f, 100f, comp, Color.fromARGB(100, 0, 0, 0).asARGB())
            Misc.sendPacket(p, dataPacket)
            textDisplayID[p.uniqueId] = id
        } else {
            val dataPacket = Misc.getUpdatePacket(textDisplayID[p.uniqueId]!!, comp)
            Misc.sendPacket(p, dataPacket)
        }
    }

    private fun spawnInteractions(p: Player, locLeft: Location, locRight: Location) {
        for (id in interactions[p.uniqueId]!!) {
            val packet = Misc.getDestroyPacket(id)
            Misc.sendPacket(p, packet)
        }
        interactions[p.uniqueId]!!.clear()

        val idLeft = LeaderboardManager.getID()
        var packet = Misc.getSpawnPacketInteraction(idLeft, locLeft)
        Misc.sendPacket(p, packet)
        interactions[p.uniqueId]!!.add(idLeft)

        val idRight = LeaderboardManager.getID()
        packet = Misc.getSpawnPacketInteraction(idRight, locRight)
        Misc.sendPacket(p, packet)
        interactions[p.uniqueId]!!.add(idRight)
    }

    fun disable() {

        interactions.entries.forEach {
            val offlinePlayer = Bukkit.getOfflinePlayer(it.key)
            if (offlinePlayer.isOnline) {
                it.value.forEach { id ->
                    val packet = Misc.getDestroyPacket(id)
                    Misc.sendPacket(Bukkit.getPlayer(it.key)!!, packet)
                }
                val packet = Misc.getDestroyPacket(textDisplayID[it.key]!!)
                Misc.sendPacket(Bukkit.getPlayer(it.key)!!, packet)
            }
        }
        taskID.forEach(Bukkit.getScheduler()::cancelTask)

        HandlerList.unregisterAll(this)
    }

    fun getLines(p: Player): MutableList<String> {
        val entries = section!!.get("entries") as Int
        val lines: MutableList<String> = mutableListOf()
        var index = (index[p.uniqueId] ?: 0)
        if (index < 0) index = placeholders.size - 1
        if (index > placeholders.size - 1) index = 0
        val players: MutableList<String> = mutableListOf()
        val scores: MutableList<String> = mutableListOf()
        var added = 0
        repeat(entries) {
            val holder = placeholders[index]
            val player = sortedPlaceholders[holder]!!.getPlayer(it) ?: return@repeat
            val score = sortedPlaceholders[holder]!!.getValue(it)
            players.add(Bukkit.getOfflinePlayer(player).name!!)
            scores.add(NumberHelper(score).toShorten())
            added += 1
        }
        if (added == 0) {
            players.add("Could be you!")
            scores.add("N/A")
        }

        if (p.name in players) {
            val format = getLeaderboardLines(true)
            val color = section!!.get("player-in-top.color-name")
            val holder = placeholders[index]
            val playerPlace = (sortedPlaceholders[holder]!!.getPlayerPosition(p.uniqueId) + 1).toString()
            for (line in format) {
                if (line == "\$stats") {
                    var place = 0
                    for (play in players) {
                        place += 1
                        var line_format = (if (section!!.isSet("format.$place")) section!!.getString("format.$place") else section!!.get("format.default", "<gray>#$place <green>\$player <gray>(\$value)"))!!.toString()
                        line_format = line_format.replace("\$value", scores[place - 1]).replace("\$place", place.toString())
                        line_format = if (play == p.name) {
                            line_format.replace("\$player", "$color${p.name}")
                        } else {
                            line_format.replace("\$player", play)
                        }
                        lines.add(line_format)
                    }
                } else {
                    lines.add(line.replace("\$refreshTime", getTimeLeft()).replace("\$place", playerPlace).replace("\$variable", names[index]).replace("\$total", sortedPlaceholders[holder]!!.getSize().toString()))
                }
            }
        } else {
            val holder = placeholders[index]
            val playerPlace = (sortedPlaceholders[holder]!!.getPlayerPosition(p.uniqueId) + 1).toString()
            val format = getLeaderboardLines(false)
            for (line in format) {
                if (line == "\$stats") {
                    var place = 0
                    for (play in players) {
                        place += 1
                        var line_format = (if (section!!.isSet("format.$place")) section!!.getString("format.$place") else section!!.get("format.default", "<gray>#$place <green>\$player <gray>(\$value)"))!!.toString()
                        line_format = line_format.replace("\$value", scores[place - 1]).replace("\$place", place.toString()).replace("\$player", play)
                        lines.add(line_format)
                    }
                } else {
                    lines.add(line.replace("\$refreshTime", getTimeLeft()).replace("\$place", playerPlace).replace("\$variable", names[index]).replace("\$total", sortedPlaceholders[holder]!!.getSize().toString()))
                }
            }
        }

        return lines
    }

    fun getLeaderboardLines(top: Boolean): MutableList<String> {
        return if (top) section!!.getStringList("player-in-top.lines") else section!!.getStringList("default.lines")
    }
}