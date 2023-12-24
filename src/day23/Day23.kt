package day23

import Input
import day
import println
import util.matrix.Direction
import util.matrix.Matrix
import util.queue.queue
import wtf

sealed class Spot(val c: Char)
data object Path : Spot('.')
data object Forest : Spot('#')
sealed class Slope(val direction: Direction, c: Char) : Spot(c)
data object UpSlope : Slope(Direction.Top, '^')
data object RightSlope : Slope(Direction.Right, '>')
data object DownSlope : Slope(Direction.Bottom, 'v')
data object LeftSlope : Slope(Direction.Left, '<')

data class Point(val type: Spot, val trails: MutableMap<Direction, Int> = mutableMapOf()) {
    fun longestTrail() = trails.maxOfOrNull { it.value }
}

fun main() = day(23) {
    fun parseMap(input: Input) = Matrix.fromLines(input, setOf(
        Path, Forest, UpSlope, RightSlope, DownSlope, LeftSlope
    ).associateBy { it.c }) { type: Spot ->
        Point(type)
    }

    part1(check = 94, ::parseMap) { map ->
        val start = map.row(0).asSequence().single { it.value.type === Path }
        val target = map.row(map.height - 1).asSequence().single { it.value.type === Path }
        target.value.trails[Direction.Bottom] = 0

        queue(listOf(target)) { field ->
            val validDirections = when (field.value.type) {
                Path -> Direction.entries - field.value.trails.keys
                is Slope -> listOf(field.value.type.direction.opposite())
                else -> wtf("There should be no ${field.value.type} in the path")
            }
            val longestTrail = field.value.trails.maxOf { it.value } + 1
            for (direction in validDirections) {
                val trailDirection = direction.opposite()
                val neighbour = field[direction]
                if (neighbour.isOutOfBounds) continue
                when (neighbour.value.type) {
                    Forest -> continue
                    is Slope -> if (neighbour.value.type.direction != trailDirection) continue
                    else -> {}
                }
                if ((neighbour.value.trails[trailDirection] ?: 0) < longestTrail) {
                    neighbour.value.trails[trailDirection] = longestTrail
                    add(neighbour)
                }
            }
        }

        val plen = map.asSequence().maxOf { it.value.longestTrail() ?: 0 }.toString().length
        map.println(printer = {
            value.type.c + (value.longestTrail()?.toString() ?: "").padStart(plen)
        })

        start.value.trails.maxOf { it.value }
    }
}