package day15

import Input
import day
import println

fun main() = day(15) {
    fun CharSequence.hash() = fold(0) { acc, c ->
        ((acc + c.code) * 17) % 256
    }

    check("HASH".hash().println() == 52)

    fun parseSequence(input: Input) = input.single().splitToSequence(',')

    part1(check = 1320, ::parseSequence) { seq ->
        seq.sumOf { it.hash() }
    }
}