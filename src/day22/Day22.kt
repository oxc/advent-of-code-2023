package day22

import Input
import day
import println
import util.matrix.Matrix3D
import util.matrix.Projection
import util.matrix.Projector3D
import util.matrix.SubMatrix3D
import util.parse.splitToInts
import wtf

data class BrickDef(val xs: IntRange, val ys: IntRange, val zs: IntRange)

data class Spot(var brick: Brick? = null) {
    fun receiveBrick(brick: Brick) {
        if (this.brick !== null) wtf("Only empty sports can receive a brick")
        this.brick = brick
    }
}

typealias Space = Matrix3D<Spot>

fun SubMatrix3D<*>.coordinates() = "[$minX,$minY,$minZ~$maxX,$maxY,$maxZ]"

data class Brick(
    val label: String,
    var fields: SubMatrix3D<Spot>,
    var hasSettledOnBricks: Set<Brick>? = null,
    val bricksHaveSettledOn: MutableSet<Brick> = mutableSetOf()
) {
    val c = when (label.length) {
        1 -> label
        else -> ('a'..'z').random().toString()
    }
    val base get() = fields.get(zs = 0..0)

    fun wouldFallTo() = fields.get(zs = -1..<(fields.sizeZ - 1))

    fun fallOne() {
        val newFields = wouldFallTo()
        fields.asSequence().forEach {
            it.value.brick = null
        }
        newFields.asSequence().forEach {
            it.value.receiveBrick(this)
        }
        fields = newFields
    }

    override fun toString(): String {
        if (hasSettledOnBricks !== null) {
            return "Brick($label, ${fields.coordinates()}, settledOn=[${hasSettledOnBricks?.joinToString { "${it.label}@${it.fields.coordinates()}" }}]"
        } else {
            return "Brick($label, ${fields.coordinates()}, still falling)"
        }
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Brick)?.let { it.label == label } ?: false
    }
}

data class Setup(val space: Space, val bricks: List<Brick>)

fun List<BrickDef>.setup(): Setup = Matrix3D.ofSize(
    minOf { it.xs.first }..maxOf { it.xs.last },
    minOf { it.ys.first }..maxOf { it.ys.last },
    0..maxOf { it.zs.last },
) {
    Spot()
}.let { matrix ->
    Setup(matrix, mapIndexed { i, brickDef ->
        val label = i.toString(26).uppercase().map {
            when (it) {
                in '0'..'9' -> 'A'.plus(it.digitToInt())
                else -> it.plus(10)
            }
        }.joinToString("")
        Brick(label, matrix[brickDef.xs, brickDef.ys, brickDef.zs]).also { brick ->
            brick.fields.asSequence().forEach { field ->
                field.value.brick = brick
                println("$field -> $brick")
            }
        }
    })
}

val debug = false
fun debug(msg: () -> String) {
    if (debug) println(msg())
}

fun List<Brick>.settle() {
    val fallingBricks = ArrayDeque(this.sortedBy { it.fields.minZ })
    while (fallingBricks.isNotEmpty()) {
        val brick = fallingBricks.removeFirst()
        if (brick.hasSettledOnBricks !== null) {
            debug { "Brick has already settled: $brick" }
            continue
        }
        if (brick.fields.minZ == 1) {
            debug { "Brick landed on the floor: $brick" }
            brick.hasSettledOnBricks = emptySet()
            continue
        }
        val bricksBelow = brick.wouldFallTo().asSequence().mapNotNullTo(mutableSetOf()) { field ->
            field.value.brick.takeIf { it !== null && it !== brick }
        }
        val needFallFirst = bricksBelow.filter { it.hasSettledOnBricks === null }
        if (needFallFirst.isNotEmpty()) {
            debug { "Brick needs other bricks falling first: $brick needs $needFallFirst" }
            fallingBricks.addAll(needFallFirst)
            fallingBricks.add(brick)
            continue
        }
        if (bricksBelow.isNotEmpty()) {
            debug { "Brick has settled: $brick settled on $bricksBelow" }
            brick.hasSettledOnBricks = bricksBelow
            bricksBelow.forEach { it.bricksHaveSettledOn += brick }
        } else {
            debug { "Brick falling one: $brick" }
            brick.fallOne()
            debug { "Brick fell one: $brick" }
            fallingBricks.add(brick)
        }
    }

}

fun Space.printProjected() {
    val projector = Projector3D<Spot> {
        val bricks = mapNotNull { it.value.brick }.take(2).toSet()
        when (bricks.size) {
            1 -> bricks.first().c
            0 -> "."
            2 -> "?"
            else -> wtf()
        }
    }
    println("Front")
    this.print(Projection.Front, projector = projector).println()
    println("Left")
    this.print(Projection.Left, projector = projector).println()
}

fun main() = day(22) {
    fun parseBricks(input: Input) = input.map { line ->
        val (start, end) = line.split('~')
        val (x1, y1, z1) = start.splitToInts(',')
        val (x2, y2, z2) = end.splitToInts(',')
        BrickDef(x1..x2, y1..y2, z1..z2)
    }.setup()


    part1(check = 5, ::parseBricks) { (matrix, bricks) ->
        bricks.forEach { println("Brick: $it") }
        matrix.printProjected()
        bricks.settle()
        bricks.forEach { println("Settled: $it") }
        matrix.printProjected()


        val couldDisintegrate = bricks.filter { brick ->
            val anyBrickRestingOnlyOnThisOne = brick.bricksHaveSettledOn.any {
                it.hasSettledOnBricks!!.singleOrNull() == brick
            }
            !anyBrickRestingOnlyOnThisOne
        }
        for (brick in couldDisintegrate) {
            println("Could disintegrate: $brick")
        }
        couldDisintegrate.size
    }
}