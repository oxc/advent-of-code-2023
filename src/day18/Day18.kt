package day18

import day
import println
import util.matrix.Direction
import util.matrix.Highlight
import util.matrix.Matrix
import wtf

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

typealias Terrain = Matrix<Point>

val debug = false

val colored = Highlight<Point> {
    (it.value.kind as? Trench)?.color?.toANSI()
}

private fun Terrain.digTrench(plan: List<DigPlanEntry>) {
    autoExpand = true
    var current = this[0, 0]
    for (step in plan) {
        if (debug) println("Digging ${step.meters} meter(s) ${step.direction}")
        repeat(step.meters) {
            current = current[step.direction]
            current.value.kind = Trench(step.color)
            if (debug) println(highlight = colored)
        }
    }
    autoExpand = false
}

private fun Terrain.fillTrench() {
    val fields = ArrayDeque(grow(1).asSequence().filter { it.isOutOfBounds }.toList())
    while (fields.isNotEmpty()) {
        val field = fields.removeFirst()
        field.value.kind = Outside
        for (neighbour in field.directNeighbours) {
            if (!neighbour.isOutOfBounds && neighbour.value.kind == null) {
                neighbour.value.kind = Outside
                fields += neighbour
            }
        }
    }
    asSequence().filter { it.value.kind == null }.forEach { it.value.kind = Inside }
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

    part1(check = 62, ::parseDigPlan) { plan ->
        val terrain = Matrix.ofSize(1, 1, ::Point)

        terrain.digTrench(plan)
        terrain.println(highlight = colored)

        terrain.fillTrench()
        terrain.println(highlight = colored)

        terrain.asSequence().count { it.value.kind != Outside }
    }
}

