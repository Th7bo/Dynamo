package com.th7bo.dynamo.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.*

/** Strings */
fun String.toComponent(): Component = FormatHelper(this).parse()

fun String.toComponent(vararg placeholders: TagResolver): Component =
    miniMessage.deserialize(this, *placeholders)

fun String.toUpperCase(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun String.toTinyString(): String = FormatHelper(this).toSmallCaps()

fun Number.fix(): String = "%.1f".format(this.toDouble())

fun Location.toPacketEvents(): com.github.retrooper.packetevents.protocol.world.Location = com.github.retrooper.packetevents.protocol.world.Location(this.x, this.y, this.z, this.yaw, this.pitch)

val EPSILON = Math.ulp(1.0) * 2.0
private fun isSignificant(value: Double): Boolean {
    return abs(value) >= EPSILON
}

fun Long.formatTime(): String {
    val days = this / (24 * 3600)
    val hours = this % (24 * 3600) / 3600
    val minutes = this % 3600 / 60
    val remainingSeconds = this % 60

    val string = buildString {
        if (days > 0) append("${days}d, ")
        if (hours > 0) append("${hours}h, ")
        if (minutes > 0) append("${minutes}m, ")
        if (remainingSeconds > 0) append("${remainingSeconds}s")
    }.trimEnd(',', ' ')
    return string
}

fun String.parseDurationToSeconds(): Int {
    var totalSeconds = 0
    val regex = "(\\d+)([dhms])".toRegex()
    regex.findAll(this).forEach { matchResult ->
        val (value, unit) = matchResult.destructured
        totalSeconds += when (unit) {
            "d" -> value.toInt() * 24 * 3600
            "h" -> value.toInt() * 3600
            "m" -> value.toInt() * 60
            "s" -> value.toInt()
            else -> 0
        }
    }
    return totalSeconds
}

fun String.asLocation(): Location {
    val split = this.split(",")
    if (split.size != 6) throw IllegalArgumentException("Invalid location string, most likely due to default config!")
    return Location(
        Bukkit.getWorld(split[0])!!,
        split[1].toDouble(),
        split[2].toDouble(),
        split[3].toDouble(),
        split[4].toFloat(),
        split[5].toFloat()
    )
}

fun getCustomRel(loc: Location, forward: Double, right: Double, up: Double): Location {
    val ret: Location = loc
    var direction: Vector? = null
    if (isSignificant(forward)) {
        direction = ret.direction
        ret.add(direction.clone().multiply(forward))
    }
    val hasUp = isSignificant(up)
    if (hasUp && direction == null) direction = ret.direction
    if (isSignificant(right) || hasUp) {
        val rightDirection: Vector
        if (direction != null && isSignificant(abs(direction.y) - 1)) {
            rightDirection = direction.clone()
            val factor = sqrt(1 - rightDirection.y.pow(2.0)) // a shortcut that lets us not normalize which is slow
            val nx: Double = -rightDirection.z / factor
            val nz: Double = rightDirection.x / factor
            rightDirection.setX(nx)
            rightDirection.setY(0.0)
            rightDirection.setZ(nz)
        } else {
            val yaw: Float = ret.getYaw() + 90f
            val yawRad = yaw * (Math.PI / 180.0)
            val z = cos(yawRad)
            val x = -sin(yawRad)
            rightDirection = Vector(x, 0.0, z)
        }
        ret.add(rightDirection.clone().multiply(right))
        if (hasUp) {
            val upDirection: Vector = rightDirection.crossProduct(direction!!)
            ret.add(upDirection.clone().multiply(up))
        }
    }
    return ret
}

fun Location.getCustomRelative(forward: Double, right: Double, up: Double): Location {
    return getCustomRel(this, forward, right, up)
}