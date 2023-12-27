package day24

import Input
import day
import util.collection.eachUniquePair
import util.parse.splitToLongs
import util.preconditions.checkEqual

data class Hailstone(val p: Point, val d: Delta) {
    override fun toString(): String {
        return "Hailstone $d @ $p"
    }
}

typealias Hailstones = List<Hailstone>

data class Point(val x: Double, val y: Double, val z: Double) {
    operator fun plus(delta: Delta) = Point(x + delta.dx, y + delta.dy, z + delta.dz)

    override fun toString(): String {
        return "($x,$y,$z)"
    }
}

data class Delta(val dx: Double, val dy: Double, val dz: Double) {
    override fun toString(): String {
        return "Δ($dx, $dy, $dz)"
    }
}

val debug = true
fun debug(msg: () -> String) {
    if (debug) println(msg())
}

private fun Hailstone.xt(t: Double) = (d.dx * t) + p.x
private fun Hailstone.yt(t: Double) = (d.dy * t) + p.y
private fun Hailstone.yx(x: Double) = (d.dy / d.dx).let { d -> x * d - p.x * d + p.y }
private fun Hailstone.tx(x: Double) = (x - p.x) / d.dx
private fun Hailstone.ty(y: Double) = (y - p.y) / d.dy
private fun Hailstones.countIntersections(area: LongRange): Int {
    return eachUniquePair().count { (a, b) ->
        // x = d.dx * t + p.x
        // t = (x - p.x) / d.dx
        // y = d.dy * t + p.y
        // t = (y - p.y) / d.dy
        // (x - p.x) / d.dx = (y - p.y) / d.dy
        // (y - p.y) = (x - p.x) / d.dx * d.dy
        // y = (x - p.x) * d.dy / d.dx + p.y
        // d := (d.dy / d.dx)
        // y = x * d - p.x * d + p.y

        // ad := (a.d.dy / a.d.dx)
        // bd := (b.d.dy / b.d.dx)

        // x * ad + (a.p.y - a.p.x * ad) = x * bd + (b.p.y - b.p.x * bd)
        // x * ad - x * bd = (b.p.y - b.p.x * bd) - (a.p.y - a.p.x * ad)
        // x * (ad - bd) =  (b.p.y - b.p.x * bd) - (a.p.y - a.p.x * ad)
        // x = ((b.p.y - b.p.x * bd) - (a.p.y - a.p.x * ad)) / (ad - bd)

        val ad = a.d.dy / a.d.dx
        val bd = b.d.dy / b.d.dx
        val x = ((b.p.y - b.p.x * bd) - (a.p.y - a.p.x * ad)) / (ad - bd)

        val y = a.yx(x)
        checkEqual(y, b.yx(x))

        val ta = a.tx(x)
        checkEqual(ta, a.ty(y))
        checkEqual(x, a.xt(ta))
        checkEqual(y, a.yt(ta))

        val tb = b.tx(x)
        checkEqual(tb, b.ty(y))
        checkEqual(x, b.xt(tb))
        checkEqual(y, b.yt(tb))

        debug { "\nHailstone A: $a\nHailstone B: $b" }

        operator fun LongRange.contains(d: Double) = d >= first && d <= last
        when {
            x.isInfinite() || y.isInfinite() -> false.also {
                debug { "Hailstones' paths are parallel; they never intersect." }
            }

            x !in area || y !in area -> false.also {
                debug { "Hailstones' paths will cross outside the test area (at x=$x, y=$y)." }
            }

            ta < 0 || tb < 0 -> false.also {
                debug {
                    "Hailstones' paths crossed in the past for ${
                        when {
                            ta < 0 && tb < 0 -> "both hailstones"
                            ta < 0 -> "hailstone A"
                            else -> "hailstone B"
                        }
                    }."
                }
            }

            else -> true.also {
                debug { "Hailstones' paths will cross inside the test area (at x=$x, y=$y)." }
            }
        }
    }
}


fun main() = day(24) {
    fun parseHail(input: Input) = input.map { line ->
        val (sPos, sVelocity) = line.split('@')
        val (x, y, z) = sPos.splitToLongs(',').map { it.toDouble() }
        val (vx, vy, vz) = sVelocity.splitToLongs(',').map { it.toDouble() }
        Hailstone(Point(x, y, z), Delta(vx, vy, vz))
    }

    part1(check = 2, ::parseHail) { hail ->
        if (hail.size == 5) {
            hail.countIntersections(7L..27L)
        } else {
            hail.countIntersections(200000000000000..400000000000000)
        }

    }
}