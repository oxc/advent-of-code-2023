package day18

import day
import println
import util.matrix.*
import util.queue.queue
import wtf
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class RGB(val red: UByte, val green: UByte, val blue: UByte) {
    fun toANSI() = "48;2;${red};${green};${blue}"
}

data class DigPlanEntry(val direction: Direction, val meters: Int, val color: RGB)

sealed interface Kind
class Trench(val color: RGB) : Kind
data object Outside : Kind
data object Inside : Kind
class Point(var kind: Kind? = null) {
    override fun toString(): String = when (kind) {
        is Trench -> "#"
        Inside -> "#"
        Outside -> "."
        null -> "?"
    }
}

typealias Terrain = SparseMatrix<Point>
typealias DigPlan = List<DigPlanEntry>
typealias Waypoints = List<Waypoint>

val debug = false

val colored = Highlight<SparseFieldData<Point>> {
    (it.innerValue.kind as? Trench)?.color?.toANSI()
}

data class Waypoint(val x: Long, val y: Long) {
    operator fun plus(delta: BaseDelta) = Waypoint(x = x + delta.x, y = y + delta.y)
}

private fun DigPlan.buildWayPoints(): Waypoints {
    var current = Waypoint(0, 0)
    val waypoints = mutableListOf(current)
    for (entry in this) {
        current += (entry.direction * entry.meters)
        waypoints += current
    }
    return waypoints.println()
}

private fun DigPlan.buildTerrain(waypoints: Waypoints): Terrain {
    val xs = waypoints.mapTo(sortedSetOf()) { it.x }.println()
    val ys = waypoints.mapTo(sortedSetOf()) { it.y }.println()
    val terrain = SparseMatrix.buildSparseMatrix(xs, ys) {
        Point()
    }
    return terrain
}

private fun Terrain.digTrench(plan: List<DigPlanEntry>, waypoints: Waypoints) {
    waypoints.zipWithNext().zip(plan) { (a, b), step ->
        if (debug) println("Digging ${step.meters} meter(s) ${step.direction}")
        val xs = min(a.x, b.x)..max(a.x, b.x)
        val ys = min(a.y, b.y)..max(a.y, b.y)
        this[xs, ys].asSequence().forEach {
            it.innerValue.kind = Trench(step.color)
            if (debug) matrix.println(highlight = colored)
        }
    }
}

private fun Terrain.fillTrench() {
    with(matrix) {
        queue(grow(1).asSequence().filter { it.isOutOfBounds }.toList()) { field ->
            field.innerValue.kind = Outside
            for (neighbour in field.directNeighbours) {
                if (!neighbour.isOutOfBounds && neighbour.innerValue.kind == null) {
                    neighbour.innerValue.kind = Outside
                    add(neighbour)
                }
            }
        }
        asSequence().filter { it.innerValue.kind == null }.forEach { it.innerValue.kind = Inside }
    }
}

fun main() = day(18) {
    fun parseDigPlan(input: List<String>) = input.map { line ->
        val (dir, m, hex) = line.split(' ')
        val direction = when (dir) {
            "U" -> Direction.Top
            "D" -> Direction.Bottom
            "L" -> Direction.Left
            "R" -> Direction.Right
            else -> wtf("Unknown direction $dir")
        }
        val meters = m.toInt()
        val (r, g, b) = hex.removePrefix("(#").removeSuffix(")").chunked(2).map { it.toUByte(16) }
        DigPlanEntry(direction, meters, RGB(r, g, b))
    }

    fun parseFixedDigPlan(input: List<String>) = input.map { line ->
        val hex = line.split('#').last()
        val meters = hex.substring(0..<5).toInt(16)
        val direction = when (hex[5]) {
            '3' -> Direction.Top
            '1' -> Direction.Bottom
            '2' -> Direction.Left
            '0' -> Direction.Right
            else -> wtf("Unknown direction $hex")
        }
        val (r, g, b) = (1..3).map { Random.nextInt(255).toUByte() }
        DigPlanEntry(direction, meters, RGB(r, g, b))
    }

    fun DigPlan.digAndCountArea(): Long {
        val waypoints = buildWayPoints()
        val terrain = buildTerrain(waypoints)

        terrain.digTrench(this, waypoints)
        terrain.println(highlight = colored)

        terrain.fillTrench()
        terrain.println(highlight = colored)

        return terrain.matrix.count { it.innerValue.kind != Outside }
    }


    part1(check = 62, ::parseDigPlan) { plan ->
        plan.digAndCountArea()
    }


    part2(check = 952408144115L, ::parseFixedDigPlan) { plan ->
        plan.digAndCountArea()
    }


}

