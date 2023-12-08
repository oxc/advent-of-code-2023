typealias Direction = Char
typealias Node = String

data class Connection(val from: Node, val toLeft: Node, val toRight: Node)
data class DesertMap(val directions: List<Direction>, val connections: Map<Node, Connection>)

fun main() = day(8) {
    fun parseMap(input: List<String>): DesertMap {
        val directions = input.first().toList()
        check(directions.all { it == 'L' || it == 'R' })
        check(input[1].isBlank())

        val connections = input.drop(2).map { line ->
            val (from, toLeft, toRight) = line.split("""\W+""".toRegex())
            Connection(from, toLeft, toRight)
        }.associateBy { it.from }
        return DesertMap(directions, connections).println()
    }

    part1(checks = mapOf("test" to 2, "test2" to 6), ::parseMap) { (directions, connections) ->
        var node = "AAA"
        var d = 0
        var steps = 0

        while (node != "ZZZ") {
            val connection = connections[node] ?: wtf("No connection for node $node")
            node = when (directions[d]) {
                'L' -> connection.toLeft
                'R' -> connection.toRight
                else -> wtf()
            }
            d = (d + 1) % directions.size
            steps += 1
        }

        steps
    }
}
