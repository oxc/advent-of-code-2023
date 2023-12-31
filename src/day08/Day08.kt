package day08

import day
import println
import util.collection.repeatIndefinitely
import util.number.lcm
import wtf

typealias Direction = Char
typealias Node = String

data class Connection(val from: Node, val toLeft: Node, val toRight: Node)
data class DesertMap(val directions: List<Direction>, val connections: Map<Node, Connection>) {
    fun nextNode(node: Node, direction: Direction): Node {
        val connection = connections[node] ?: wtf("No connection for node $node")
        return when (direction) {
            'L' -> connection.toLeft
            'R' -> connection.toRight
            else -> wtf()
        }
    }
}

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

    part1(checks = mapOf("test" to 2, "test2" to 6), ::parseMap) { map ->
        var node = "AAA"
        var d = 0
        var steps = 0

        while (node != "ZZZ") {
            node = map.nextNode(node, map.directions[d])
            d = (d + 1) % map.directions.size
            steps += 1
        }

        steps
    }

    part2(checks = mapOf("test3" to 6L), ::parseMap) { map ->
        data class Cycle(
            val cycleStart: Int,
            val cycleLength: Int,
            val targetIndex: Int,
        )

        fun Node.findCycle(): Cycle {
            val dirs = map.directions.repeatIndefinitely()
            val wrap = map.directions.size

            val waypoints = mutableListOf(this)
            var node = this
            dirs.forEachIndexed { i, direction ->
                node = map.nextNode(node, direction)
                waypoints += node
                val steps = i + 1
                for (ix in steps.downTo(0).step(wrap).drop(1)) {
                    if (waypoints[ix] == node) {
                        val cycleLength = (steps - ix)
                        val cycleOffset = ix % wrap
                        val targetIndex = (waypoints.indexOfLast { it.endsWith('Z') } - cycleOffset) % cycleLength
                        return Cycle(cycleOffset, cycleLength, targetIndex).println()
                    }
                }
            }
            wtf()
        }

        val startNodes = map.connections.keys.filter { it.endsWith('A') }
        val cycles = startNodes.map { it.findCycle() }

        val offsets = cycles.map {
            (it.targetIndex + it.cycleStart).toLong()
        }.filter { it != 0L }
        val cycleLengths = cycles.map { it.cycleLength.toLong() }
        (cycleLengths + offsets).println().fold(1L, ::lcm)
    }
}
