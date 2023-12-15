package day15

import Input
import day
import println


sealed interface Instruction {
    val label: String
}

data class RemoveInstruction(override val label: String) : Instruction
data class AddInstruction(override val label: String, val focalLength: Int) : Instruction

data class Lens(val label: String, val focalLength: Int)

typealias Box = MutableList<Lens>


fun main() = day(15) {
    fun CharSequence.hash() = fold(0) { acc, c ->
        ((acc + c.code) * 17) % 256
    }

    check("HASH".hash().println() == 52)

    fun parseSequence(input: Input) = input.single().splitToSequence(',')

    part1(check = 1320, ::parseSequence) { seq ->
        seq.sumOf { it.hash() }
    }

    fun parseInstructions(input: Input) = parseSequence(input).map { seq ->
        val (label, focalLength) = seq.split('-', '=')
        if (focalLength.isEmpty()) {
            RemoveInstruction(label)
        } else {
            AddInstruction(label, focalLength.toInt())
        }
    }

    part2(check = 145L, ::parseInstructions) { init ->
        val boxes = Array<Box>(256) { mutableListOf() }

        init.forEach { inst ->
            val bucket = inst.label.hash()
            val box = boxes[bucket]
            when (inst) {
                is AddInstruction -> {
                    val lens = Lens(inst.label, inst.focalLength)
                    box.indexOfFirst { it.label == inst.label }.takeIf { it != -1 }?.let {
                        box[it] = lens
                    } ?: run {
                        box.add(lens)
                    }
                }

                is RemoveInstruction -> {
                    box.removeAll { it.label == inst.label }
                }
            }
        }

        boxes.withIndex().sumOf { (index, box) ->
            (index + 1) * box.withIndex().sumOf { (lensSlot, lens) ->
                (lensSlot + 1) * lens.focalLength.toLong()
            }
        }
    }
}