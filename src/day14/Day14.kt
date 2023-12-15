package day14

import day
import println
import util.matrix.Direction
import util.matrix.Field
import util.matrix.Matrix

typealias Dish = Matrix<Point>
typealias DishPoint = Field<Point>

data class Point(var value: Char) {
    override fun toString() = value.toString()
}

fun DishPoint.roll(direction: Direction) {
    if (this.value.value != 'O') {
        return
    }
    var roller = this
    while (true) {
        val target = roller[direction]
        if (target.isOutOfBounds) return
        if (target.value.value != '.') return
        roller.value.value = target.value.value
        target.value.value = 'O'
        roller = target
    }
}

fun Dish.tilt(direction: Direction) {
    val yRange = when (direction) {
        Direction.Top -> 1..<height
        Direction.Bottom -> (height - 2).downTo(0)
        else -> null
    }
    val xRange = when (direction) {
        Direction.Left -> 1..<width
        Direction.Right -> (width - 2).downTo(0)
        else -> null
    }
    when {
        yRange !== null ->
            for (y in yRange) {
                for (x in 0..<width) {
                    this[x, y].roll(direction)
                }
            }

        xRange !== null ->
            for (x in xRange) {
                for (y in 0..<height) {
                    this[x, y].roll(direction)
                }
            }
    }
}

fun Dish.spin() {
    tilt(Direction.Top)
    tilt(Direction.Left)
    tilt(Direction.Bottom)
    tilt(Direction.Right)
}

fun Dish.load() = asSequence().filter { it.value.value == 'O' }.sumOf { point ->
    height - point.y
}


fun main() = day(14) {

    fun parseDish(input: List<String>) = Matrix.fromLines(input) { Point(it) }

    part1(check = 136, ::parseDish) { dish ->
        dish.println().tilt(Direction.Top)
        dish.println()

        dish.load()
    }

    part2(check = 64, ::parseDish) { dish ->
        val loads = mutableListOf<Int>()

        fun findCycleLength(): Int {
            spin@ do {
                dish.spin()
                dish.println()
                loads += dish.load().println()

                // calculate some rounds
                if (loads.size < 100) continue

                for (i in 1..(loads.size / 3)) {
                    val (a, b, c) = loads.reversed().chunked(i)
                    if (a == b && b == c) {
                        return a.size
                    }
                }
            } while (true)
        }

        val cycleLength = findCycleLength()

        val offset = (1_000_000_000 - loads.size - 1) % cycleLength

        val cycle = loads.takeLast(cycleLength).println()
        cycle[offset]
    }
}