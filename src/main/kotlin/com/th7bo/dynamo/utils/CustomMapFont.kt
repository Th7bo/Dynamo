package com.th7bo.dynamo.utils

import org.bukkit.ChatColor

// modified from CraftBukkit
open class CustomMapFont {
    private val chars = HashMap<Char, CharacterSprite?>()

    var height: Int = 0
        private set
    protected var malleable: Boolean = true

    fun setChar(ch: Char, sprite: CharacterSprite) {
        check(malleable) { "this font is not malleable" }

        chars[ch] = sprite
        if (sprite.height > height) {
            height = sprite.height
        }
    }

    fun getChar(ch: Char): CharacterSprite? {
        return chars[ch]
    }

    fun getWidth(text: String): Int {
        if (text.isEmpty()) {
            return 0
        }

        var result = 0
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch == ChatColor.COLOR_CHAR) {
                val j = text.indexOf(';', i)
                if (j >= 0) {
                    i = j
                    ++i
                    continue
                }
                throw IllegalArgumentException("Text contains unterminated color string")
            }
            result += chars[ch]?.width ?: 2 // we do allow custom character widths
            ++i
        }
        result += text.length - 1

        return result
    }

    class CharacterSprite(
        val width: Int,
        val height: Int, private val data: BooleanArray
    ) {
        init {
            require(data.size == width * height) { "size of data does not match dimensions" }
        }
        fun get(row: Int, col: Int): Boolean {
            if (row < 0 || col < 0 || row >= height || col >= width) return false
            return data[row * width + col]
        }
    }
}