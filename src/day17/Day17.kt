package day17

import day
import println
import util.matrix.Direction
import util.matrix.Field
import util.matrix.Highlight
import util.matrix.Matrix

typealias HeatMap = Matrix<Point>
typealias HeatMapPoint = Field<Point>

data class RouteStart(val direction: Direction, val stepsInThatDirection: Int) {
    override fun toString(): String = "${direction.arrow}${stepsInThatDirection}"
}

class Point(val heatLoss: Int, val routes: MutableMap<RouteStart, Int> = mutableMapOf()) {
    override fun toString() = heatLoss.toString()

    fun setRouteIfShorter(route: RouteStart, value: Int): Boolean {
        val existing = routes[route]
        if (existing != null && existing <= value) return false
        routes[route] = value
        return true
    }
}

val debug = false

val HeatMap.target get() = this[width - 1, height - 1]
fun HeatMap.init(
    minInOneDirection: Int,
    maxInOneDirection: Int
) {
    val fillQueue = ArrayDeque<HeatMapPoint>()
    with(target) {
        for (direction in Direction.entries) {
            for (steps in minInOneDirection..maxInOneDirection) {
                value.routes[RouteStart(direction, steps)] = 0
            }
        }
    }
    fillNeighbours(target, fillQueue, minInOneDirection, maxInOneDirection)
    while (fillQueue.isNotEmpty()) {
        val field = fillQueue.removeFirst()
        fillNeighbours(field, fillQueue, minInOneDirection, maxInOneDirection)
    }
}

fun HeatMap.fillNeighbours(
    field: HeatMapPoint,
    queue: ArrayDeque<HeatMapPoint>,
    minInOneDirection: Int,
    maxInOneDirection: Int
) = with(field.value) {
    val updatedNeighbours = mutableListOf<HeatMapPoint>()

    fun HeatMapPoint.setRoute(routeStart: RouteStart, route: Int): Boolean {
        val routeUpdated = value.setRouteIfShorter(routeStart, route)
        if (routeUpdated) {
            if (debug) println("New shortest route from $this arriving $routeStart is $route")
            updatedNeighbours += this
        }
        return routeUpdated
    }
    for (neighborDir in Direction.entries) {
        val neighbor = field[neighborDir].takeUnless { it.isOutOfBounds } ?: continue
        if (neighbor == target) continue
        val dir = neighborDir.opposite()
        for (i in 1..<maxInOneDirection) {
            val nextRoute = routes[RouteStart(dir, i + 1)] ?: continue
            neighbor.setRoute(RouteStart(dir, i), heatLoss + nextRoute)
        }
        val shortest = routes[RouteStart(dir, 1)] ?: continue
        val newRoute = shortest + heatLoss
        for (otherDir in listOf(dir.previous(), dir.next())) {
            for (steps in minInOneDirection..maxInOneDirection) {
                neighbor.setRoute(RouteStart(otherDir, steps), newRoute)
            }
        }
    }

    if (updatedNeighbours.isNotEmpty()) {
        queue.addAll(updatedNeighbours)
    }
}

fun HeatMap.printShortestRoutes(highlight: Highlight<Point> = Highlight.none()) {
    println(printer = {
        (value.routes.values.minOrNull()?.toString() ?: "?").padStart(4)
    }, highlight)
}

fun main() = day(17) {

    fun parseHeatMap(input: List<String>) = Matrix.fromLines(input, Point(-1)) {
        Point(it.digitToInt())
    }

    fun HeatMap.shortestRoute(): Int? {
        // apparently, the crucible needs to start to the right. Didn't find that in the description
        // otherwise it would be:
        // this[0, 0].value.entries.filter { it.key.stepsInThatDirection == 1 }.minOf { it.value }
        return this[0, 0].value.routes[(RouteStart(Direction.Right, 1))]
    }

    part1(check = 102, ::parseHeatMap) { map ->
        map.init(1, 3)
        map.printShortestRoutes()

        map.shortestRoute()
    }

    part2(checks = mapOf("test2" to 71, "test" to 94), ::parseHeatMap) { map ->
        map.init(4, 10)
        map.printShortestRoutes()

        map.shortestRoute()
    }

}
