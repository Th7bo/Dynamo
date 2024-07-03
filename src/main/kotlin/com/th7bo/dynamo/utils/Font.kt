package com.th7bo.dynamo.utils

object Font {
    private val font = CustomFont(true)
    fun getWidth(s: String): Int {
        return font.getWidth(s)
    }
}