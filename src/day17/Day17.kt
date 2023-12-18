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
        if (debug) println("New shortest route from $this arriving $route is $value")
        return true
    }
}

val debug = false

val HeatMap.target get() = this[width - 1, height - 1]
fun HeatMap.init() {
    val fillQueue = ArrayDeque<HeatMapPoint>()
    with(target) {
        for (direction in Direction.entries) {
            for (steps in 1..2) {
                value.routes[RouteStart(direction, steps)] = 0
            }
        }
    }
    fillNeighbours(target, fillQueue)
    while (fillQueue.isNotEmpty()) {
        val field = fillQueue.removeFirst()
        fillNeighbours(field, fillQueue)
    }
}

fun HeatMap.fillNeighbours(field: HeatMapPoint, queue: ArrayDeque<HeatMapPoint>) = with(field.value) {
    val updatedNeighbours = mutableListOf<HeatMapPoint>()

    fun HeatMapPoint.setRoute(routeStart: RouteStart, route: Int): Boolean {
        val routeUpdated = value.setRouteIfShorter(routeStart, route)
        if (routeUpdated) {
            updatedNeighbours += this
        }
        return routeUpdated
    }
    for (neighborDir in Direction.entries) {
        val neighbor = field[neighborDir].takeUnless { it.isOutOfBounds } ?: continue
        val dir = neighborDir.opposite()
        for (i in 2..3) {
            val nextRoute = routes[RouteStart(dir, i)] ?: continue
            neighbor.setRoute(RouteStart(dir, i - 1), heatLoss + nextRoute)
        }
        val shortest = routes[RouteStart(dir, 1)] ?: continue
        val newRoute = shortest + heatLoss
        for (otherDir in listOf(dir.previous(), dir.next())) {
            for (steps in 1..3) {
                neighbor.setRoute(RouteStart(otherDir, steps), newRoute)
            }
        }
    }

    if (updatedNeighbours.isNotEmpty()) {
        queue.addAll(updatedNeighbours)
        if (debug) {
            routes.println()
            printShortestRoutes {
                when {
                    it == field -> "41;1"
                    it in updatedNeighbours -> "42;1"
                    it.value.routes.isNotEmpty() -> "43;1"
                    else -> null
                }

            }
        }
    }
}

fun HeatMap.printShortestRoutes(highlight: Highlight<Point> = Highlight.none()) {
    println(printer = {
        (routes.values.minOrNull()?.toString() ?: "?").padStart(4)
    }, highlight)
}

fun main() = day(17) {

    fun parseHeatMap(input: List<String>) = Matrix.fromLines(input, Point(-1)) {
        Point(it.digitToInt())
    }

    part1(check = 102, ::parseHeatMap) { map ->
        map.init()
        map.printShortestRoutes()
        map[0, 0].value.routes.values.min()
    }
}
