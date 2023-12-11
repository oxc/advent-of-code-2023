package day11

import day
import util.collection.eachUniquePair
import util.matrix.AbstractMatrixElement
import util.matrix.Matrix
import kotlin.math.absoluteValue

data class UniPoint(val value: Char, var realX: Long = -1, var realY: Long = -1) {
    val isGalaxy = value == '#'
    val isVoid = value != '#'
    fun distance(other: UniPoint) = (realX - other.realX).absoluteValue + (realY - other.realY).absoluteValue
}
typealias Universe = Matrix<UniPoint>

fun main() = day(11) {
    fun parseUniverse(lines: List<String>) = Matrix.fromLines(lines) { UniPoint(it) }.apply {
        asSequence().forEach {
            it.value.realX = it.x.toLong()
            it.value.realY = it.y.toLong()
        }
    }

    fun Universe.expand(count: Long = 1L) {
        fun List<AbstractMatrixElement<UniPoint>>.expand(add: Universe.(i: Int) -> Unit) {
            forEachIndexed { i, line ->
                if (line.asSequence().all { it.value.isVoid }) {
                    add(i)
                }
            }
        }

        rows().toList().expand { y ->
            this[0..<width, y + 1..<height].asSequence().forEach {
                it.value.realY += count
            }
        }
        columns().toList().expand { x ->
            this[x + 1..<width, 0..<height].asSequence().forEach {
                it.value.realX += count
            }
        }
    }

    fun Universe.galaxies() = asSequence().filter { it.value.isGalaxy }

    fun Universe.solve(growth: Long): Long {
        expand(growth - 1)

        return galaxies().toList().eachUniquePair().sumOf { (a, b) ->
            a.value.distance(b.value)
        }
    }

    part1(check = 374L, ::parseUniverse) { universe ->
        universe.solve(2)
    }

    part2(checks = mapOf(), ::parseUniverse) { universe ->
        //universe.solve(10) // check 1030L
        universe.solve(1_000_000) // check 8410L
    }


}