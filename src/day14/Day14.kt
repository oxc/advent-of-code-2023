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

}