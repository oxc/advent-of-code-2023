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
        if (debug && field.x == 0 && field.y == 0) {
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

fun Point.validRouteStarts(): List<Map.Entry<RouteStart, Int>> {
    return routes.entries.filter { it.key.stepsInThatDirection == 1 }
}

fun HeatMap.printShortestRoutes(highlight: Highlight<Point> = Highlight.none()) {
    println(printer = {
        (value.routes.values.minOrNull()?.toString() ?: "?").padStart(4)
    }, highlight)
}

data class RoutePoint(
    val field: HeatMapPoint,
    var choice: RouteStart,
    val choicesRemaining: MutableList<RouteStart> = mutableListOf()
) {
    fun nextChoice(): Boolean {
        choice = choicesRemaining.removeFirstOrNull() ?: return false
        return true
    }
}

fun HeatMap.printOneShortestRoute(steps: IntRange): Int {
    val seenFields = linkedMapOf<HeatMapPoint, RoutePoint>()
    val waypoints = ArrayDeque<RoutePoint>()
    val results = mutableListOf<Int>()
    fun p() {
        val current = waypoints.last().field
        println(printer = {
            seenFields[this]?.let {
                "${
                    value.routes[it.choice].toString().padStart(4)
                }${it.choice.direction.arrow}${it.choice.stepsInThatDirection}"
            }
                ?: ((value.routes.values.minOrNull()?.toString() ?: "?").padStart(4) + "  ")
        }, {
            when {
                it == current -> "41;1"
                it in seenFields -> "43;1"
                else -> null
            }
        })
    }

    val target = this.target
    with(this[0, 0]) {
        val choices = value.validRouteStarts()
            .sortedBy { it.value }.println().mapTo(mutableListOf()) { it.key }
        val choice = choices.removeFirst()
        RoutePoint(this, choice, choices).also {
            waypoints += it
            seenFields[this] = it
        }
    }

    tailrec fun backtrack() {
        println("backtrack")
        p()
        if (waypoints.last().nextChoice()) {
            return
        }
        waypoints.removeLast().also {
            seenFields.remove(it.field)
        }
        backtrack()
    }

    fun RouteStart.goOn() = copy(stepsInThatDirection = stepsInThatDirection + 1)
    fun RouteStart.turnLeft() = RouteStart(direction = direction.previous(), stepsInThatDirection = 1)
    fun RouteStart.turnRight() = RouteStart(direction = direction.next(), stepsInThatDirection = 1)

    do {
        val current = waypoints.last()
        val comingFrom = current.choice
        val nextField = current.field[comingFrom.direction]

        if (nextField == target) {
            val totalLoss = waypoints.drop(1).map { it.field.value.heatLoss } + target.value.heatLoss
            totalLoss.mapIndexed { i, _ -> totalLoss.drop(i).sum() }.println()
            results += totalLoss.println().sum().println()
            p()
            break
        }

        val validStarts = when {
            comingFrom.stepsInThatDirection < steps.first -> listOf(comingFrom.goOn())
            else -> listOf(comingFrom.goOn(), comingFrom.turnLeft(), comingFrom.turnRight())
        }
        val choices = validStarts.mapNotNull { routeStart ->
            nextField.value.routes[routeStart]?.let { routeStart to it }
        }
            .filterNot { nextField[it.first.direction].isOutOfBounds }
            .sortedWith(compareBy<Pair<RouteStart, Int>> { it.second }.thenBy { it.first.direction })
            .println()
            .mapTo(mutableListOf()) { it.first }

        if (choices.isEmpty()) {
            backtrack()
            continue
        }

        val cycle = seenFields[nextField]
        if (cycle != null) {
            backtrack()
            continue
        }

        val next = RoutePoint(nextField, choices.removeFirst(), choices)
        seenFields[nextField] = next
        waypoints += next

        p()
    } while (waypoints.isNotEmpty())

    return results.println().min().println()
}

fun main() = day(17) {

    fun parseHeatMap(input: List<String>) = Matrix.fromLines(input, Point(-1)) {
        Point(it.digitToInt())
    }

    fun HeatMap.shortestRoute(): Int? {
        // apparently, the crucible needs to start to the right. Didn't find that in the description
        // otherwise it would be:
        // this[0, 0].value.validRouteStarts().minOf { it.value }
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
