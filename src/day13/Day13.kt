package day13

import day
import println
import util.collection.eachUniquePair
import util.matrix.Field
import util.matrix.Matrix
import util.parse.split
import wtf

typealias Pattern = Matrix<Point>

data class Point(val value: Char, var isSmudge: Boolean = false) {
    fun mirrors(other: Point): Boolean {
        return isSmudge || other.isSmudge || value == other.value
    }

    override fun toString() = if (isSmudge) "?" else value.toString()
}

fun main() = day(13) {

    fun parsePatterns(input: List<String>) = input.split { it.isBlank() }.map { lines ->
        Matrix.fromLines(lines) { Point(it) }
    }

    fun Pattern.linesEqual(x1: Int, x2: Int, line: Pattern.(Int) -> Sequence<Field<Point>>): Boolean =
        line(x1).zip(line(x2)).all { (a, b) -> a.value.mirrors(b.value) }

    fun Pattern.findReflections(baseRange: IntRange, line: Pattern.(Int) -> Sequence<Field<Point>>): List<Int> {
        return baseRange.zipWithNext().mapNotNull { (i1, i2) ->
            if (linesEqual(i1, i2, line)) {
                val iBefore = (i1 - 1).downTo(0)
                val iAfter = (i2 + 1)..baseRange.last
                if (iBefore.zip(iAfter).all { (b, a) -> linesEqual(b, a, line) }) {
                    i1 + 1
                } else null
            } else null
        }
    }

    fun Pattern.findVerticalReflections() = findReflections(0..<width) { column(it).asSequence() }
    fun Pattern.findHorizontalReflections() = findReflections(0..<height) { row(it).asSequence() }

    fun Pattern.findReflections() =
        findVerticalReflections() + findHorizontalReflections().map { it * 100 }


    part1(check = 405, ::parsePatterns) { patterns ->
        patterns.mapIndexed { index, pattern ->
            pattern.findReflections().singleOrNull() ?: wtf("No single reflection for pattern $index")
        }.println().sum()
    }

    part2(check = 400, ::parsePatterns) { patterns ->
        fun Pattern.findSmudge(baseRange: IntRange, line: Pattern.(Int) -> Sequence<Field<Point>>) =
            baseRange.eachUniquePair().mapNotNull smudge@{ (x1, x2) ->
                var mismatch: Field<Point>? = null
                for ((a, b) in line(x1).zip(line(x2))) {
                    if (a.value != b.value) {
                        if (mismatch != null) return@smudge null
                        mismatch = a
                    }
                }
                mismatch
            }.toList()


        fun Pattern.findVerticalSmudge() = findSmudge(0..<width) { column(it).asSequence() }

        fun Pattern.findHorizontalSmudge() = findSmudge(0..<height) { row(it).asSequence() }

        fun Pattern.findPotentialSmudges() = findVerticalSmudge() + findHorizontalSmudge()

        patterns.mapIndexed { index, pattern ->
            val clearReflection =
                pattern.findReflections().singleOrNull() ?: wtf("No single clear reflection for $index")
            pattern.findPotentialSmudges().firstNotNullOfOrNull { smudge ->
                smudge.value.isSmudge = true
                pattern.findReflections().filter { it != clearReflection }.also {
                    pattern.println()
                    if (it.isNotEmpty()) {
                        println("Pattern $index has smudged reflections at $it")
                    } else {
                        println("Pattern $index has no reflection with smudge $smudge")
                    }
                }.also {
                    smudge.value.isSmudge = false
                }.singleOrNull()
            } ?: wtf("No smudged reflection found for $index")
        }.println().sum()
    }


}