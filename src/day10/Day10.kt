package day10

import day
import println
import util.matrix.Field
import util.matrix.Matrix
import wtf
import java.util.*

typealias M = Matrix<PipeField>
typealias F = Field<PipeField>
typealias Connections = Pair<F, F>

sealed class Pipe(val input: Char, val pretty: Char = input, val prettyUnknown: Char = pretty)

data object Ground : Pipe('.', ' ')
data object Start : Pipe('S')

sealed class ConnectingPipe(
    input: Char,
    pretty: Char,
    prettyUnknown: Char,
    private val direction: F.() -> Connections
) :
    Pipe(input, pretty, prettyUnknown) {
    fun connections(field: F) = field.direction()
}

// directions must be CCW
data object VerticalPipe : ConnectingPipe('|', '║', '│', { bottom to top })
data object HorizontalPipe : ConnectingPipe('-', '═', '─', { left to right })
data object NorthEast : ConnectingPipe('L', '╚', '└', { right to top })
data object NorthWest : ConnectingPipe('J', '╝', '┘', { top to left })
data object SouthWest : ConnectingPipe('7', '╗', '┐', { left to bottom })
data object SouthEast : ConnectingPipe('F', '╔', '┌', { bottom to right })

enum class Location { Inside, Outside, Loop, Unknown }
data class PipeField(val pipe: Pipe, var location: Location = Location.Unknown) {
    override fun toString() = when (location) {
        Location.Loop -> pipe.pretty.toString()
        Location.Inside -> "I"
        Location.Outside -> "O"
        Location.Unknown -> pipe.prettyUnknown.toString()
    }
}

fun main() = day(10) {
    val byInput = listOf(
        Ground, Start, VerticalPipe, HorizontalPipe, NorthEast, NorthWest, SouthWest, SouthEast
    ).associateBy { it.input }

    fun parseMatrix(input: List<String>) = Matrix.fromLines(input, PipeField(Ground, Location.Outside)) {
        val pipe = byInput[it] ?: wtf("Unexpected char $it")
        PipeField(pipe)
    }

    fun Connections.other(from: F): F? {
        if (first == from) return second
        if (second == from) return first
        return null
    }


    fun F.connections() = (value.pipe as? ConnectingPipe)?.connections(this)

    fun M.findLoop(): List<F> {
        fun trace(start: F, firstNeighbour: F): List<F>? {
            val path = mutableListOf(start)
            start.value.location = Location.Loop
            var current = start
            var next = firstNeighbour
            do {
                val connections = next.connections()
                val newNext = connections?.other(current) ?: return null
                next.value.location = Location.Loop
                path += next
                current = next
                next = newNext
            } while (next != start)
            return path
        }

        val start = asSequence().find { it.value.pipe == Start } ?: wtf("No start")

        val traces = start.directNeighbours.mapNotNull { trace(start, it) }
        check(traces.size == 2) { "Expected 2 traces, but found $traces" }
        check(traces[0].size == traces[1].size)
        return traces.first()
    }

    part1(
        checks = mapOf(
            "test1_plain" to 4, "test1_filled" to 4,
            "test2_plain" to 8, "test2_filled" to 8
        ), ::parseMatrix
    ) { matrix ->
        val loop = matrix.findLoop()
        matrix.println()

        loop.size / 2
    }

    part2(
        checks = mapOf(
            "test3" to 4, "test4" to 4,
            "test5" to 8,
            "test6" to 10,
        ), ::parseMatrix
    ) { matrix ->
        val loop = matrix.findLoop()
        matrix.println()

        val outsides = LinkedList(matrix.grow(1).asSequence().filter { it.value.location == Location.Outside }.toList())

        fun F.markAsOutsideIfUnknown() {
            if (value.location == Location.Unknown) {
                value.location = Location.Outside
                outsides.add(this)
            }
        }

        fun markOutsides() {
            while (outsides.isNotEmpty()) {
                val field = outsides.pop()
                field.directNeighbours.forEach { neighbour ->
                    neighbour.markAsOutsideIfUnknown()
                }
            }
        }
        markOutsides()

        fun Pair<F, F>.isRegularDirection() = first == second.connections()?.first

        fun Pair<F, F>.clockwiseLeftOfLoop(): List<F> {
            return second.run {
                val pipe = value.pipe as? ConnectingPipe ?: return emptyList()

                if (isRegularDirection()) {
                    when (pipe) {
                        VerticalPipe -> listOf(left)
                        HorizontalPipe -> listOf(top)
                        NorthEast -> listOf(bottom, bottomLeft, left)
                        NorthWest -> listOf(right, bottomRight, bottom)
                        SouthWest -> listOf(top, topRight, right)
                        SouthEast -> listOf(left, topLeft, top)
                    }
                } else {
                    when (pipe) {
                        VerticalPipe -> listOf(right)
                        HorizontalPipe -> listOf(bottom)
                        NorthEast -> listOf(topRight)
                        NorthWest -> listOf(topLeft)
                        SouthWest -> listOf(bottomLeft)
                        SouthEast -> listOf(bottomRight)
                    }
                }
            }
        }

        val clockwiseLeftOutsideLoop = loop.zipWithNext()
            .first { (_, second) -> second.directNeighbours.any { it.value.location == Location.Outside } }
            .run {

                // we know this field has an outside neighbor, so if any of the left ones is outside, all are
                val leftIsOutside = clockwiseLeftOfLoop().any { it.value.location == Location.Outside }

                // we want to mark all left fields as outside, so reverse the loop if necessary
                if (leftIsOutside) loop else loop.reversed()
            }

        clockwiseLeftOutsideLoop.zipWithNext().forEach { loopField ->
            loopField.clockwiseLeftOfLoop().forEach {
                it.markAsOutsideIfUnknown()
            }
        }
        markOutsides()


        matrix.asSequence().filter { it.value.location == Location.Unknown }
            .forEach { it.value.location = Location.Inside }

        matrix.println().asSequence().count { it.value.location == Location.Inside }

    }
}
