package day23

import Input
import day
import println
import util.matrix.*
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

sealed interface Point {
    val type: Spot
    fun longestTrail(): Int?
}

data class Part1Point(override val type: Spot, val trails: MutableMap<Direction, Int> = mutableMapOf()) : Point {
    override fun longestTrail() = trails.maxOfOrNull { it.value }
}

data class Part2Point(override val type: Spot, val trails: MutableSet<Trail> = mutableSetOf()) : Point {
    override fun longestTrail() = trails.maxOfOrNull { it.size }
}

data class Trail(val head: Field<Part2Point>, val tail: Trail?) {
    val size: Int = (tail?.size ?: 0) + 1

    operator fun plus(head: Field<Part2Point>) = Trail(head, this)

    fun asSequence() = sequence {
        var current: Trail? = this@Trail
        while (current != null) {
            yield(current.head)
            current = current.tail
        }
    }

    operator fun contains(element: Field<Part2Point>): Boolean = element in asSequence()
}

private fun <P : Point> Matrix<P>.printTrails(
    printNumbers: Boolean = true,
    highlight: Highlight<P> = Highlight.none()
) {
    val printer: Printer<P> = if (!printNumbers) {
        Printer { value.type.c.toString() }
    } else {
        val plen = asSequence().maxOf { it.value.longestTrail() ?: 0 }.toString().length
        Printer {
            value.type.c + (value.longestTrail()?.toString() ?: "").padStart(plen)
        }
    }
    println(printer = printer, highlight = highlight)
}

fun main() = day(23) {
    fun <P : Point> parseMap(mapper: (Spot) -> P): (Input) -> Matrix<P> = { input: Input ->
        Matrix.fromLines(input, setOf(
            Path, Forest, UpSlope, RightSlope, DownSlope, LeftSlope
        ).associateBy { it.c }) { spot: Spot -> mapper(spot) }
    }

    part1(check = 94, parseMap(::Part1Point)) { map ->
        val start = map.row(0).asSequence().single { it.value.type === Path }
        val target = map.row(map.height - 1).asSequence().single { it.value.type === Path }
        target.value.trails[Direction.Bottom] = 0

        queue(listOf(target)) { field ->
            val validDirections = when (field.value.type) {
                Path -> Direction.entries - field.value.trails.keys
                is Slope -> listOf(field.value.type.direction.opposite())
                else -> wtf("There should be no ${field.value.type} in the path")
            }
            val longestTrail = field.value.longestTrail()!! + 1
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

        map.printTrails()

        start.value.longestTrail()!!
    }

    part2(check = 154, parseMap(::Part2Point)) { map ->
        val start = map.row(0).asSequence().single { it.value.type === Path }
        val target = map.row(map.height - 1).asSequence().single { it.value.type === Path }
        target.value.trails += Trail(target, null)

        var lastQ = -1
        queue(listOf(target)) { field ->
            if (field === start) {
                map.printTrails(false) { f ->
                    when {
                        any { it === f } -> "43;1"
                        else -> null
                    }
                }
                println("Longest trail so far: ${start.value.longestTrail()}")
            }
            for (direction in Direction.entries) {
                val neighbour = field[direction]
                when {
                    neighbour.isOutOfBounds -> continue
                    neighbour.value.type == Forest -> continue
                    else -> {
                        val newTrails = field.value.trails.filterNot {
                            neighbour in it
                        }.map { it + neighbour }
                        val added = newTrails.fold(false) { added, trail ->
                            neighbour.value.trails.add(trail) || added
                        }
                        if (added) {
                            add(neighbour)
                        }
                    }
                }
            }
            val newQ = size / 100
            if (newQ != lastQ) {
                println("Queued: $size")
                lastQ = newQ
            }
        }

        map.printTrails()

        start.value.longestTrail()!! - 1
    }
}