package com.th7bo.dynamo.gui

import com.th7bo.dynamo.Dynamo.Companion.instance
import com.th7bo.dynamo.managers.LeaderboardManager
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import com.th7bo.dynamo.utils.Misc
import com.th7bo.dynamo.utils.format
import com.th7bo.dynamo.utils.parseDurationToSeconds
import com.th7bo.dynamo.utils.toTinyString
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.BaseGui
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

object LeaderboardsGUI {

    class PlaceholdersGUI(private var p: Player) {
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Placeholders</main>".parse()).rows(5).create()

        init {
            val addItem = ItemBuilder.from(Material.NAME_TAG).name("<green>Add Placeholder".parse()).asGuiItem() {
                p.closeInventory()
                p.sendMessage("Enter a placeholder! (eg: %plugin_value%)".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    val trimmed = value.trim().replace("%", "")
                    val papi = PlaceholderAPI.setPlaceholders(p, "%$trimmed%").replace("%", "").replace(",", "").replace("$", "")
                    var valid = true
                    try {
                        val parsed = papi.toDouble()
                    } catch (e: NumberFormatException) {
                        valid = false
                    }
                    if (!valid) {
                        Misc.error(p, "This placeholder does not seem to return a valid number! ($papi)")
                        GuiManager.openPlaceholdersGUI(p)
                        return@Consumer
                    }
                    GuiManager.consumers.remove(p.uniqueId)
                    GuiManager.builders[p.uniqueId]!!.placeholders += trimmed
                    GuiManager.openPlaceholdersGUI(p)
                }
            }
            gui.filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem())

            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val backItem = ItemBuilder.from(Material.RED_DYE).name("<red>Back".parse()).asGuiItem() {
                GuiManager.openCreateGUI(p, 2)
            }
            gui.setItem(5, 1, backItem)

            gui.setItem(5, 5, addItem)
            for ((index, format) in builder.placeholders.withIndex()) {
                val item = ItemBuilder.from(Material.PAPER).name("<main>Placeholder $index</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: $format".parse(), "".parse(), "<dark_gray>Left click to edit".parse(), "<red>Right click to delete!".parse()).asGuiItem() { event ->
                    if (event.isRightClick) {
                        builder.placeholders.removeAt(index)
                        GuiManager.openPlaceholdersGUI(p)
                    } else {
                        p.closeInventory()
                        p.sendMessage("Enter a placeholder!".parse(true))
                        GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                            GuiManager.consumers.remove(p.uniqueId)

                            val trimmed = value.trim().replace("%", "")
                            val papi = PlaceholderAPI.setPlaceholders(p, "%$trimmed%").replace("%", "").replace(",", "").replace("$", "")
                            var valid = true
                            try {
                                val parsed = papi.toDouble()
                            } catch (e: NumberFormatException) {
                                valid = false
                            }
                            if (!valid) {
                                Misc.error(p, "This placeholder does not seem to return a valid number! ($papi)")
                                GuiManager.openPlaceholdersGUI(p)
                                return@Consumer
                            }
                            GuiManager.consumers.remove(p.uniqueId)
                            GuiManager.builders[p.uniqueId]!!.placeholders[index] = trimmed
                            GuiManager.openPlaceholdersGUI(p)
                        }
                    }
                }
                gui.addItem(item)
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    class PlayerInTopLinesGUI(private var p: Player) {
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Top Lines</main>".parse()).rows(5).create()

        init {
            val addItem = ItemBuilder.from(Material.NAME_TAG).name("<green>Add Line".parse()).asGuiItem() {
                p.closeInventory()
                p.sendMessage("Enter a line! (use {empty} for empty line)".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    if (value == "{empty}") {
                        builder.playerInTopLines += ""
                    } else {
                        builder.playerInTopLines += value
                    }
                    GuiManager.consumers.remove(p.uniqueId)
                    GuiManager.openTopLinesGUI(p)
                }
            }

            val addEmptyItem = ItemBuilder.from(Material.BARRIER).name("<green>Add Empty Line".parse()).asGuiItem() {
                builder.playerInTopLines += ""
                GuiManager.openTopLinesGUI(p)
            }
            gui.filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem())

            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val backItem = ItemBuilder.from(Material.RED_DYE).name("<red>Back".parse()).asGuiItem() {
                GuiManager.openCreateGUI(p, 4)
            }
            gui.setItem(5, 1, backItem)

            gui.setItem(5, 4, addItem)
            gui.setItem(5, 6, addEmptyItem)
            for ((index, format) in builder.playerInTopLines.withIndex()) {
                val item = ItemBuilder.from(Material.PAPER).name("<main>Line $index</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: $format".parse(), "".parse(), "<dark_gray>Left click to edit".parse(), "<red>Right click to delete!".parse()).asGuiItem() { event ->
                    if (event.isRightClick) {
                        builder.playerInTopLines.removeAt(index)
                        GuiManager.openTopLinesGUI(p)
                    } else {
                        p.closeInventory()
                        p.sendMessage("Enter a line! (use {empty} for empty line)".parse(true))
                        GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                            GuiManager.consumers.remove(p.uniqueId)
                            if (value == "{empty}") {
                                builder.playerInTopLines[index] = ""
                            } else {
                                builder.playerInTopLines[index] = value
                            }
                            GuiManager.openTopLinesGUI(p)
                        }
                    }
                }
                gui.addItem(item)
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    class DefaultLinesGUI(private var p: Player) {
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Default Lines</main>".parse()).rows(5).create()

        init {
            val addItem = ItemBuilder.from(Material.NAME_TAG).name("<green>Add Line".parse()).asGuiItem() {
                p.closeInventory()
                p.sendMessage("Enter a line! (use {empty} for empty line)".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    if (value == "{empty}") {
                        builder.defaultLines += ""
                    } else {
                        builder.defaultLines += value
                    }
                    GuiManager.consumers.remove(p.uniqueId)
                    GuiManager.openDefaultLinesGUI(p)
                }
            }

            val addEmptyItem = ItemBuilder.from(Material.BARRIER).name("<green>Add Empty Line".parse()).asGuiItem() {
                builder.defaultLines += ""
                GuiManager.openDefaultLinesGUI(p)
            }
            gui.filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem())

            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val backItem = ItemBuilder.from(Material.RED_DYE).name("<red>Back".parse()).asGuiItem() {
                GuiManager.openCreateGUI(p, 4)
            }
            gui.setItem(5, 1, backItem)

            gui.setItem(5, 4, addItem)
            gui.setItem(5, 6, addEmptyItem)
            for ((index, format) in builder.defaultLines.withIndex()) {
                val item = ItemBuilder.from(Material.PAPER).name("<main>Line $index</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: $format".parse(), "".parse(), "<dark_gray>Left click to edit".parse(), "<red>Right click to delete!".parse()).asGuiItem() { event ->
                    if (event.isRightClick) {
                        builder.defaultLines.removeAt(index)
                        GuiManager.openDefaultLinesGUI(p)
                    } else {
                        p.closeInventory()
                        p.sendMessage("Enter a line! (use {empty} for empty line)".parse(true))
                        GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                            GuiManager.consumers.remove(p.uniqueId)
                            if (value == "{empty}") {
                                builder.defaultLines[index] = ""
                            } else {
                                builder.defaultLines[index] = value
                            }
                            GuiManager.openDefaultLinesGUI(p)
                        }
                    }
                }
                gui.addItem(item)
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    class NamesGUI(private var p: Player, private var page: Int = 1) {
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Names</main>".parse()).rows(5).create()

        init {
            val addItem = ItemBuilder.from(Material.NAME_TAG).name("<green>Add Name".parse()).asGuiItem() {
                p.closeInventory()
                p.sendMessage("Enter a name! (eg: Blocks Broken)".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    GuiManager.consumers.remove(p.uniqueId)
                    builder.names += value
                    GuiManager.openNamesGUI(p)
                }
            }
            gui.filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem())

            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val backItem = ItemBuilder.from(Material.RED_DYE).name("<red>Back".parse()).asGuiItem() {
                GuiManager.openCreateGUI(p, 2)
            }
            gui.setItem(5, 1, backItem)

            gui.setItem(5, 5, addItem)
            for ((index, format) in builder.names.withIndex()) {
                val item = ItemBuilder.from(Material.PAPER).name("<main>Name $index</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: $format".parse(), "".parse(), "<dark_gray>Left click to edit".parse(), "<red>Right click to delete!".parse()).asGuiItem() { event ->
                    if (event.isRightClick) {
                        builder.names.removeAt(index)
                        GuiManager.openNamesGUI(p)
                    } else {
                        p.closeInventory()
                        p.sendMessage("Enter a name!".parse(true))
                        GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                            GuiManager.consumers.remove(p.uniqueId)
                            builder.names[index] = value
                            GuiManager.openNamesGUI(p)
                        }
                    }
                }
                gui.addItem(item)
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    class FormatGUI(private var p: Player, private var page: Int = 1) {
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Formats</main>".parse()).rows(5).create()

        init {
            val addItem = ItemBuilder.from(Material.NAME_TAG).name("<green>Add Format".parse()).asGuiItem() {
                p.closeInventory()
                p.sendMessage("Enter a string! (eg: 1;;format)".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    GuiManager.consumers.remove(p.uniqueId)
                    val split = value.split(";;")
                    val index = split[0].toIntOrNull()
                    if (index == null || index <= 0) {
                        Misc.error(p, "Invalid index!")
                        GuiManager.openFormatGUI(p)
                        return@Consumer
                    }
                    val format = split[1]
                    builder.formats[index] = format
                    GuiManager.openFormatGUI(p)
                }
            }
            gui.filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem())
            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val backItem = ItemBuilder.from(Material.RED_DYE).name("<red>Back".parse()).asGuiItem() {
                GuiManager.openCreateGUI(p, 3)
            }
            gui.setItem(5, 1, backItem)

            gui.setItem(5, 5, addItem)

            val defaultItem = ItemBuilder.from(Material.ANVIL).name("<main>Default format</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: ${builder.defaultFormat ?: "Not set"}".parse(), "".parse(), "<dark_gray>Left click to edit".parse()).asGuiItem() { event ->
                p.closeInventory()
                p.sendMessage("Enter a string!".parse(true))
                GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                    GuiManager.consumers.remove(p.uniqueId)
                    builder.defaultFormat = value
                    GuiManager.openFormatGUI(p)
                }
            }
            gui.addItem(defaultItem)
            for ((index, format) in builder.formats) {
                val item = ItemBuilder.from(Material.PAPER).name("<main>Format $index</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: $format".parse(), "".parse(), "<dark_gray>Left click to edit".parse(), "<red>Right click to delete!".parse()).asGuiItem() { event ->
                    if (event.isRightClick) {
                        builder.formats.remove(index)
                        GuiManager.openFormatGUI(p)
                    } else {
                        p.closeInventory()
                        p.sendMessage("Enter a string!".parse(true))
                        GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                            GuiManager.consumers.remove(p.uniqueId)
                            builder.formats[index] = value
                            GuiManager.openFormatGUI(p)
                        }
                    }
                }
                gui.addItem(item)
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    class CreateGUI(private var p: Player, private var page: Int = 1) {
        private val pageAmount = 4
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Setup</main> <gray>($page/$pageAmount)".parse()).rows(3).create()
        private val nameItem: GuiItem = ItemBuilder.from(Material.NAME_TAG).name("<main>Key</main>".parse()).lore(("<dark_gray>" + "text".toTinyString()).parse(), "".parse(), "<gray>Current key: ${builder.key ?: "Not set"}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Enter a key! (no special characters)".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.key = value
                GuiManager.openCreateGUI(p, page)
            }
        }
        private val dynamicItem: GuiItem = ItemBuilder.from(Material.AMETHYST_SHARD).name("<main>Dynamic Leaderboard</main>".parse()).lore(("<dark_gray>" + "boolean".toTinyString()).parse(), "".parse(), "<gray>Current value: ${builder.dynamic}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Enter a boolean! (invalid input = false)".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.dynamic = (value == "true")
                GuiManager.openCreateGUI(p, page)
            }
        }

        private val locationItem: GuiItem = ItemBuilder.from(Material.COMPASS).name("<main>Location</main>".parse()).lore(("<dark_gray>" + "location".toTinyString()).parse(), "".parse(), "<gray>Current location: ${builder.location?.format()}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Type confirm to accept your current location!".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> {
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.location = p.location
                GuiManager.openCreateGUI(p, page)
            }
        }

        private val refreshTimeItem: GuiItem = ItemBuilder.from(Material.CLOCK).name("<main>Refresh Time</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: ${builder.refreshTime ?: "Not Set"}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Enter a string! (eg: 10m30s)".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                val parsed = value.parseDurationToSeconds()
                if (parsed <= 0) {
                    Misc.error(p, "Time cannot be negative or 0!")
                    GuiManager.openCreateGUI(p, page)
                    return@Consumer
                }
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.refreshTime = value
                GuiManager.openCreateGUI(p, page)
            }
        }

        private val placeholderItem: GuiItem = ItemBuilder.from(Material.LOOM).name("<main>Placeholders</main>".parse()).lore("".parse(), "<gray>Click me to open the builder!".parse()).asGuiItem() {
            GuiManager.openPlaceholdersGUI(p)
        }

        private val namesItem: GuiItem = ItemBuilder.from(Material.OAK_SIGN).name("<main>Placeholder Names</main>".parse()).lore("".parse(), "<gray>Click me to open the builder!".parse()).asGuiItem() {
            GuiManager.openNamesGUI(p)
        }

        private val entriesItem: GuiItem = ItemBuilder.from(Material.TARGET).name("<main>Entries</main>".parse()).lore(("<dark_gray>" + "integer".toTinyString()).parse(), "".parse(), "<gray>Current value: ${builder.entries}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Enter the amount of entries that show up on the leaderboard!".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                val entries = value.toIntOrNull()
                if (entries == null || entries <= 0) {
                    Misc.error(p, "Invalid number!")
                    GuiManager.openCreateGUI(p, page)
                    return@Consumer
                }
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.entries = entries
                GuiManager.openCreateGUI(p, page)
            }
        }

        private val topColorItem: GuiItem = ItemBuilder.from(Material.BLACK_DYE).name("<main>Top color</main>".parse()).lore(("<dark_gray>" + "string".toTinyString()).parse(), "".parse(), "<gray>Current value: ${builder.topColor ?: "Not set"}".parse()).asGuiItem() {
            p.closeInventory()
            p.sendMessage("Enter the color overlay you would see your own name if you are on the leaderboard!".parse(true))
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                GuiManager.consumers.remove(p.uniqueId)
                GuiManager.builders[p.uniqueId]!!.topColor = value
                GuiManager.openCreateGUI(p, page)
            }
        }

        private val formatsItem: GuiItem = ItemBuilder.from(Material.LADDER).name("<main>Formatting</main>".parse()).lore("".parse(), "<gray>Click me to open the builder!".parse()).asGuiItem() {
            GuiManager.openFormatGUI(p)
        }
        private val defaultLinesItem: GuiItem = ItemBuilder.from(Material.LIGHT_GRAY_DYE).name("<main>Default lines</main>".parse()).lore("".parse(), "<gray>Click me to open the builder!".parse()).asGuiItem() {
            GuiManager.openDefaultLinesGUI(p)
        }
        private val playerInTopLinesItem: GuiItem = ItemBuilder.from(Material.GREEN_DYE).name("<main>Player in top lines</main>".parse()).lore("".parse(), "<gray>Click me to open the builder!".parse()).asGuiItem() {
            GuiManager.openTopLinesGUI(p)
        }
        private val tryBuild: GuiItem = ItemBuilder.from(Material.EMERALD).name("<main>Build</main>".parse()).lore("".parse(), "<gray>Click me to build the leaderboard!".parse()).asGuiItem()
        private var invalidBuild = ItemBuilder.from(Material.RED_DYE).name("<dark_red>Invalid build".parse()).lore("".parse(), "<gray>There are missing fields!".parse()).asGuiItem()

        private val page1: MutableMap<Int, GuiItem> = if (!builder.editing) mutableMapOf(11 to nameItem, 13 to dynamicItem, 15 to locationItem) else mutableMapOf(12 to dynamicItem, 14 to locationItem)
        private val page2: MutableMap<Int, GuiItem> = mutableMapOf(11 to refreshTimeItem, 13 to placeholderItem, 15 to namesItem)
        private val page3: MutableMap<Int, GuiItem> = mutableMapOf(11 to entriesItem, 13 to formatsItem, 15 to topColorItem)
        private val page4: MutableMap<Int, GuiItem> = mutableMapOf(11 to defaultLinesItem, 13 to playerInTopLinesItem, 15 to tryBuild)

        private val pages: MutableList<MutableMap<Int, GuiItem>> = mutableListOf(page1, page2, page3, page4)

        init {
            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val noPageBefore = ItemBuilder.from(Material.GRAY_DYE).name("<dark_red>Error".parse()).lore("".parse(), "<gray>There is no page before this.".parse()).asGuiItem()
            val noPageAfter  = ItemBuilder.from(Material.GRAY_DYE).name("<dark_red>Error".parse()).lore("".parse(), "<gray>There is no page after this." .parse()).asGuiItem()

            val nextPage = ItemBuilder.from(Material.GREEN_DYE).name("<green>Next page".parse()).asGuiItem()
            val prevPage = ItemBuilder.from(Material.RED_DYE).name("<red>Previous page".parse()).asGuiItem()

            registerSlotAction(gui, 8, prevPage, noPageBefore, 30, { page > 1 }) { GuiManager.openCreateGUI(p, page - 1) }
            registerSlotAction(gui, 26, nextPage, noPageAfter, 30, { page < pageAmount }) { GuiManager.openCreateGUI(p, page + 1) }

            gui.setItem(8, prevPage)
            gui.setItem(26, nextPage)

            val filler = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem()
            gui.filler.fill(filler)
            if (page <= pages.size) {
                for ((slot, item) in pages[page - 1]) {
                    gui.setItem(slot, item)
                }
            }
            if (page == 4) {
                registerSlotAction(gui, 15, tryBuild, invalidBuild, 30, { builder.isValidConfiguration(p) }) {
                    val lb = builder.build(p)
                    lb?.save()
                    LeaderboardManager.leaderboards[lb!!.key] = lb.init()
                    p.sendMessage("Leaderboard built!".parse(true))
                    GuiManager.builders.remove(p.uniqueId)
                }
            }
        }

        fun open() {
            gui.open(p)
        }
    }

    fun registerSlotAction(gui: BaseGui, slot: Int, oldItem: GuiItem, newItem: GuiItem, delay: Long, requiredCode: () -> Boolean, action: () -> Unit) {
        gui.addSlotAction(slot) { event ->
            if (event.currentItem == oldItem.itemStack && !requiredCode()) {
                gui.updateItem(slot, newItem)
                object : BukkitRunnable() {
                    override fun run() {
                        gui.updateItem(slot, oldItem)
                    }
                }.runTaskLater(instance, delay)
            } else if (event.currentItem != newItem.itemStack) {
                action()
            }
        }
    }

}