typealias F = Field<Pipe>
typealias Connections = Pair<F, F>

sealed class Pipe(val input: Char, val pretty: Char) {
    override fun toString() = pretty.toString()
}

object Ground : Pipe('.', ' ')
object Start : Pipe('S', 'S')

sealed class ConnectingPipe(input: Char, pretty: Char, private val direction: F.() -> Connections) :
    Pipe(input, pretty) {
    fun connections(field: F) = field.direction()
}

object VerticalPipe : ConnectingPipe('|', '║', { top to bottom })
object HorizontalPipe : ConnectingPipe('-', '═', { left to right })
object NorthEast : ConnectingPipe('L', '╚', { top to right })
object NorthWest : ConnectingPipe('J', '╝', { top to left })
object SouthWest : ConnectingPipe('7', '╗', { bottom to left })
object SouthEast : ConnectingPipe('F', '╔', { bottom to right })

fun main() = day(10) {
    val byInput = listOf(
        Ground, Start, VerticalPipe, HorizontalPipe, NorthEast, NorthWest, SouthWest, SouthEast
    ).associateBy { it.input }

    fun parseMatrix(input: List<String>) = Matrix.fromLines(input) { byInput[it] ?: wtf("Unexpected char $it") }

    fun Connections.other(from: F): F? {
        if (first == from) return second
        if (second == from) return first
        return null
    }


    fun F.connections() = (value as? ConnectingPipe)?.connections(this)

    fun Matrix<Pipe>.findLoop(): List<F> {
        fun trace(start: F, firstNeighbour: F): List<F>? {
            val path = mutableListOf(start)
            var current = start
            var next = firstNeighbour
            do {
                val connections = next.connections()
                val newNext = connections?.other(current) ?: return null
                path += next
                current = next
                next = newNext
            } while (next != start)
            return path
        }

        val start = asSequence().find { it.value == Start } ?: wtf("No start")

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
        val loop = matrix.println().findLoop()

        loop.size / 2
    }
}
