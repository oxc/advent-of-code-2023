package day13

import day
import println
import util.matrix.CharMatrix
import util.parse.parseCharMatrix
import util.parse.split
import wtf

typealias Pattern = CharMatrix

fun main() = day(13) {

    fun parsePatterns(input: List<String>) = input.split { it.isBlank() }.map { parseCharMatrix(it) }

    fun Pattern.vlinesEqual(x1: Int, x2: Int): Boolean =
        column(x1).asSequence().zip(column(x2).asSequence()).all { (a, b) -> a.value == b.value }

    fun Pattern.hlinesEqual(y1: Int, y2: Int): Boolean =
        row(y1).asSequence().zip(row(y2).asSequence()).all { (a, b) -> a.value == b.value }

    fun Pattern.findVerticalReflection(): Int? {
        (0..width).zipWithNext().forEach { (x1, x2) ->
            if (vlinesEqual(x1, x2)) {
                println("VLines $x1 and $x2 equal")
                val xLeft = (x1 - 1).downTo(0)
                val xRight = (x2 + 1)..maxX
                if (xLeft.zip(xRight).all { (l, r) -> vlinesEqual(l, r) }) {
                    return x1 + 1
                }
            }
        }
        return null
    }

    fun Pattern.findHorizontalReflection(): Int? {
        (0..<height).zipWithNext().forEach { (y1, y2) ->
            if (hlinesEqual(y1, y2)) {
                println("HLines $y1 and $y2 equal")
                val yLeft = (y1 - 1).downTo(0)
                val yRight = (y2 + 1)..maxY
                if (yLeft.zip(yRight).all { (l, r) -> hlinesEqual(l, r) }) {
                    return (y1 + 1) * 100
                }
            }
        }
        return null
    }


    part1(check = 405, ::parsePatterns) { patterns ->
        patterns.mapIndexed { index, pattern ->
            pattern.findVerticalReflection()
                ?: pattern.findHorizontalReflection()
                ?: wtf("No reflection for pattern $index")
        }.println().sum()
    }


}