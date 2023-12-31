package day16

import day
import println
import util.matrix.Direction
import util.matrix.Field
import util.matrix.Matrix
import util.queue.queue
import wtf

typealias Contraption = Matrix<Point>
typealias ContraPoint = Field<Point>

sealed class Thing(val c: Char) {
    abstract fun guideLight(direction: Direction): List<Direction>

    override fun toString(): String = c.toString()
}

object Soil : Thing('.') {
    override fun guideLight(direction: Direction) = listOf(direction)
}

sealed class Mirror(c: Char) : Thing(c) {
    abstract fun reflectLight(direction: Direction): Direction

    override fun guideLight(direction: Direction) = listOf(reflectLight(direction))
}

object Slash : Mirror('/') {
    override fun reflectLight(direction: Direction) = when (direction) {
        Direction.Right -> Direction.Top
        Direction.Top -> Direction.Right
        Direction.Bottom -> Direction.Left
        Direction.Left -> Direction.Bottom
    }
}

object Backslash : Mirror('\\') {
    override fun reflectLight(direction: Direction) = when (direction) {
        Direction.Right -> Direction.Bottom
        Direction.Bottom -> Direction.Right
        Direction.Top -> Direction.Left
        Direction.Left -> Direction.Top
    }
}

object VSplitter : Thing('|') {
    override fun guideLight(direction: Direction) = when (direction) {
        Direction.Left, Direction.Right -> listOf(Direction.Top, Direction.Bottom)
        else -> listOf(direction)
    }
}

object HSplitter : Thing('-') {
    override fun guideLight(direction: Direction) = when (direction) {
        Direction.Top, Direction.Bottom -> listOf(Direction.Left, Direction.Right)
        else -> listOf(direction)
    }
}

class Point(var content: Thing, var energy: Int = 0, val beams: MutableMap<Direction, Boolean> = mutableMapOf()) {
    override fun toString() = content.toString()

    fun hasBeam(direction: Direction) = beams.getOrDefault(direction, false)

    fun beam(direction: Direction) {
        energy += 1
        beams[direction] = true
    }

    fun reset() {
        energy = 0
        beams.clear()
    }
}

val debug = false

private fun Matrix<Point>.sendLight(x: Int, y: Int, initialDir: Direction) {
    queue(listOf(this[x, y] to initialDir)) { (field, direction) ->
        if (field.value.hasBeam(direction)) return@queue
        field.value.beam(direction)
        field.value.content.guideLight(direction).forEach { dir ->
            field[dir].takeUnless { it.isOutOfBounds }?.let {
                add(it to dir)
            }
        }
        if (debug) {
            println(this)
            this@sendLight.println {
                when {
                    any { beam -> beam.first == it } -> "41;1"
                    it.value.energy > 0 -> "43;1"
                    it.value.energy > 1 -> "42;1"
                    else -> null
                }
            }
        }
    }
}

fun Contraption.reset() {
    asSequence().forEach {
        it.value.reset()
    }
}

fun Contraption.fieldsEnergized() = asSequence().count { it.value.energy > 0 }

fun main() = day(16) {

    fun parseContraption(input: List<String>) = Matrix.fromLines(input) {
        Point(
            when (it) {
                Soil.c -> Soil
                Slash.c -> Slash
                Backslash.c -> Backslash
                VSplitter.c -> VSplitter
                HSplitter.c -> HSplitter
                else -> wtf("Unexpected char $it")
            }
        )
    }

    part1(check = 46, ::parseContraption) { contraption ->
        contraption.sendLight(0, 0, Direction.Right)

        contraption.fieldsEnergized()
    }

    part2(check = 51, ::parseContraption) { contraption ->
        sequenceOf(
            contraption.row(0) to Direction.Bottom,
            contraption.column(0) to Direction.Right,
            contraption.row(contraption.height - 1) to Direction.Top,
            contraption.column(contraption.width - 1) to Direction.Left,
        ).flatMap { (elems, direction) -> elems.asSequence().map { it to direction } }
            .maxOf { (field, direction) ->
                contraption.sendLight(field.x, field.y, direction)
                contraption.fieldsEnergized().also {
                    contraption.reset()
                }
            }
    }
}
