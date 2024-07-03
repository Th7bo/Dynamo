package com.th7bo.dynamo.utils

import java.util.regex.Pattern


// taken from Skript
class Version {
    private val version = arrayOfNulls<Int>(3)
    private val postfix: String?

    constructor(vararg version: Int) {
        require(!(version.isEmpty() || version.size > 3)) { "Versions must have a minimum of 2 and a maximum of 3 numbers (" + version.size + " numbers given)" }
        for (i in version.indices) this.version[i] = version[i]
        postfix = null
    }

    constructor(major: Int, minor: Int, postfix: String?) {
        version[0] = major
        version[1] = minor
        this.postfix = if (postfix.isNullOrEmpty()) null else postfix
    }

    constructor(version: String) {
        val m = versionPattern.matcher(version.trim { it <= ' ' })
        require(m.matches()) { "'$version' is not a valid version string" }
        for (i in 0..2) {
            if (m.group(i + 1) != null) this.version[i] = parseInt("" + m.group(i + 1))
        }
        postfix = m.group(4)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) return false
        return compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return version.contentHashCode() * 31 + (postfix?.hashCode() ?: 0)
    }

    fun compareTo(other: Version?): Int {
        if (other == null) return 1

        for (i in version.indices) {
            if (get(i) > other.get(i)) return 1
            if (get(i) < other.get(i)) return -1
        }

        if (postfix == null) return if (other.postfix == null) 0 else 1
        return if (other.postfix == null) -1 else postfix.compareTo(other.postfix)
    }

    fun compareTo(vararg other: Int): Int {
        assert(other.size in 2..3)
        for (i in version.indices) {
            if (get(i) > (if (i >= other.size) 0 else other[i])) return 1
            if (get(i) < (if (i >= other.size) 0 else other[i])) return -1
        }
        return 0
    }

    private fun get(i: Int): Int {
        return if (version[i] == null) 0 else version[i]!!
    }

    fun isSmallerThan(other: Version): Boolean {
        return compareTo(other) < 0
    }

    fun isLargerThan(other: Version): Boolean {
        return compareTo(other) > 0
    }

    val isStable: Boolean
        get() = postfix == null

    val major: Int
        get() = version[0]!!

    val minor: Int
        get() = version[1]!!

    val revision: Int
        get() = if (version[2] == null) 0 else version[2]!!

    override fun toString(): String {
        return version[0].toString() + "." + version[1] + (if (version[2] == null) "" else "." + version[2]) + (if (postfix == null) "" else "-$postfix")
    }

    companion object {
        val versionPattern: Pattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?")

        fun compare(v1: String, v2: String): Int {
            return Version(v1).compareTo(Version(v2))
        }
    }

    private fun parseInt(s: String): Int {
        assert(s.matches("-?\\d+".toRegex()))
        return try {
            s.toInt()
        } catch (e: NumberFormatException) {
            if (s.startsWith("-")) Int.MIN_VALUE else Int.MAX_VALUE
        }
    }
}