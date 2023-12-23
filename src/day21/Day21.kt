package day21

import Input
import day
import util.matrix.Direction
import util.matrix.Field
import util.matrix.Matrix
import util.matrix.Point
import util.number.absmod
import wtf

sealed class Spot(val c: Char) {
    override fun toString(): String = c.toString()
}

data object Rock : Spot('#')
data object Plot : Spot('.')

typealias Garden = Matrix<Spot>
typealias GardenSpot = Field<Spot>

private fun Garden.countReachableInSteps(goal: Int, start: Point): Int {
    var stepsLeft = goal
    var plotsReachable = setOf(this[start])
    while (stepsLeft-- > 0) {
        plotsReachable = plotsReachable.flatMapTo(mutableSetOf()) { plot ->
            plot.directNeighbours.filter { it.value !== Rock }
        }
    }
    return plotsReachable.size
}

private fun Garden.getSpotType(point: Point): Spot {
    val x = point.x absmod width
    val y = point.y absmod height
    return this[x, y].value
}

private fun Garden.countInfiniteReachableInSteps(goal: Int, start: Point): Int {
    var stepsLeft = goal
    var plotsReachable = setOf(start)
    while (stepsLeft-- > 0) {
        plotsReachable = plotsReachable.flatMapTo(mutableSetOf()) { plot ->
            Direction.entries.map { dir ->
                (plot + dir)
            }.filter { getSpotType(it) === Plot }
        }
        if (stepsLeft % 1000 == 0) {
            println("[${1 - (stepsLeft / goal) * 100}%] $stepsLeft/$goal left")
        }
    }
    return plotsReachable.size
}


fun main() = day(21) {
    fun parseMatrix(input: Input): Pair<Garden, Point> {
        lateinit var start: Point
        return Matrix.fromLinesIndexed(input) { point, c ->
            when (c) {
                '.' -> Plot
                '#' -> Rock
                'S' -> Plot.also {
                    start = point
                }

                else -> wtf("Unexpected char $c")
            }
        } to start
    }

    part1(check = 16, ::parseMatrix) { (garden, start) ->
        // small hack for the check
        val goal = if (garden.width == 11) 6 else 64

        garden.countReachableInSteps(goal, start)
    }

    part2(check = 1594, ::parseMatrix) { (garden, start) ->
        // small hack for the check
        val goal = if (garden.width == 11) 50 else 26501365

        garden.countInfiniteReachableInSteps(goal, start)
    }
}