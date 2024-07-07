package com.th7bo.dynamo.gui

import com.th7bo.dynamo.Dynamo.Companion.instance
import com.th7bo.dynamo.utils.FormatHelper.Companion.parse
import com.th7bo.dynamo.utils.format
import com.th7bo.dynamo.utils.toTinyString
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.BaseGui
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

object LeaderboardsGUI {

    var names: List<String> = listOf()
    var refreshTime: String? = null
    var placeholders: List<String> = listOf()
    var entries = 10
    var formats: MutableMap<Int, String> = mutableMapOf()
    var defaultFormat: String? = null
    var topColor: String? = null
    var playerInTopLines: MutableList<String> = mutableListOf()
    var defaultLines: MutableList<String> = mutableListOf()

    class CreateGUI(private var p: Player, private var page : Int = 1) {
        private val pages = 5
        private val builder = GuiManager.builders[p.uniqueId]!!
        private val gui = Gui.gui().title("<main>Leaderboard Setup</main> <gray>($page/$pages)".parse()).rows(3).create()
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
            GuiManager.consumers[p.uniqueId] = Consumer<String> { value ->
                if (value.lowercase() != "confirm") {
                    p.sendMessage("Invalid input!".parse(true))
                } else {
                    GuiManager.consumers.remove(p.uniqueId)
                    GuiManager.builders[p.uniqueId]!!.location = p.location
                    GuiManager.openCreateGUI(p, page)
                }
            }
        }

        private val page1: MutableMap<Int, GuiItem> = mutableMapOf(11 to nameItem, 13 to dynamicItem, 15 to locationItem)

        init {
            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            val noPageBefore = ItemBuilder.from(Material.GRAY_DYE).name("<dark_red>Error".parse()).lore("".parse(), "<gray>There is no page before this.".parse()).asGuiItem()
            val noPageAfter  = ItemBuilder.from(Material.GRAY_DYE).name("<dark_red>Error".parse()).lore("".parse(), "<gray>There is no page after this." .parse()).asGuiItem()

            val nextPage = ItemBuilder.from(Material.GREEN_DYE).name("<green>Next page".parse()).asGuiItem()
            val prevPage = ItemBuilder.from(Material.RED_DYE).name("<red>Previous page".parse()).asGuiItem()

            registerSlotAction(gui, 8, prevPage, noPageBefore, 30, { page > 1 }) { GuiManager.openCreateGUI(p, page - 1) }
            registerSlotAction(gui, 26, nextPage, noPageAfter, 30, { page < pages }) { GuiManager.openCreateGUI(p, page + 1) }

            gui.setItem(8, prevPage)
            gui.setItem(26, nextPage)

            val filler = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name("".parse()).asGuiItem()
            gui.filler.fill(filler)

            if (page == 1) {
                for ((slot, item) in page1) {
                    gui.setItem(slot, item)
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