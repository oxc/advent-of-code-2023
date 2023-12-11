package day11

import day
import println
import util.collection.eachUniquePair
import util.matrix.AbstractMatrixElement
import util.matrix.CharMatrix
import util.parse.parseCharMatrix

typealias Universe = CharMatrix

fun main() = day(11) {
    fun Universe.expand() {
        fun List<AbstractMatrixElement<Char>>.expand(add: Universe.(i: Int) -> Unit) {
            var i = 0
            var added = 0
            while (i < size) {
                if (this[i].asSequence().all { it.value != '#' }) {
                    add(i + added)
                    added += 1
                }
                i += 1
            }
        }

        rows().toList().expand { y -> insertRowAfter(y, '_') }
        columns().toList().expand { x -> insertColumnAfter(x, ',') }
    }

    fun Universe.galaxies() = asSequence().filter { it.value == '#' }

    part1(check = 374, ::parseCharMatrix) { universe ->
        universe.println()
        universe.expand()
        universe.println()

        universe.galaxies().toList().eachUniquePair().sumOf { (a, b) ->
            (a - b).distance()
        }
    }
}