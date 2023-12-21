package day19

import Input
import day
import println
import util.collection.productOf
import util.number.span
import util.parse.split
import wtf
import java.util.*

sealed interface Action
sealed interface TerminalAction : Action
data object Accept : TerminalAction
data object Reject : TerminalAction
data class Jump(val workflow: String) : Action

sealed class Operator(val f: (Long, Long) -> Boolean, val rangeF: (LongRange, Long) -> Boolean)
data object GreaterThan : Operator({ a, b -> a > b }, { vs, v -> vs.first > v })
data object LessThan : Operator({ a, b -> a < b }, { vs, v -> vs.last < v })


enum class Category { x, m, a, s }

data class Rule(val category: Category, val op: Operator, val comparisonValue: Long, val action: Action)
data class Workflow(val id: String, val rules: List<Rule>, val defaultAction: Action)

typealias Workflows = SortedMap<String, Workflow>

sealed interface Part
data class ListedPart(val values: Map<Category, Long>) : Part
data class PartRange(val ranges: Map<Category, LongRange>) : Part

fun main() = day(19) {
    fun parseAction(input: String): Action = when (input) {
        "A" -> Accept
        "R" -> Reject
        else -> Jump(input)
    }

    fun parseWorkflows(input: Input): Workflows = input.map { line ->
        val (label, rulesString) = line.removeSuffix("}").split('{')
        val sRules = rulesString.split(',')
        val rules = sRules.dropLast(1).map { rule ->
            val category = Category.valueOf(rule[0].toString())
            val op = when (rule[1]) {
                '<' -> LessThan
                '>' -> GreaterThan
                else -> wtf("Unknown operator ${rule[1]}")
            }
            val (sValue, sAction) = rule.substring(2).split(':')
            val comparisonValue = sValue.toLong()
            val action = parseAction(sAction)
            Rule(category, op, comparisonValue, action)
        }
        val defaultAction = parseAction(sRules.last())
        label to Workflow(label, rules, defaultAction)
    }.toMap(sortedMapOf())

    fun parseParts(input: Input) = input.map { line ->
        line.removeSurrounding("{", "}").split(',').map {
            val (sCat, sVal) = it.split('=')
            Category.valueOf(sCat) to sVal.toLong()
        }.toMap().let { ListedPart(it) }
    }

    fun parseWorkflowAndParts(input: Input): Pair<Workflows, List<ListedPart>> {
        val (inWorkflow, inParts) = input.split { it.isBlank() }
        val workflows = parseWorkflows(inWorkflow)
        val parts = parseParts(inParts)
        return workflows to parts
    }

    fun Rule.apply(part: Part): Action? = when (part) {
        is ListedPart -> {
            val partValue = part.values[category] ?: 0
            if (op.f(partValue, comparisonValue)) action else null
        }

        is PartRange -> {
            val partValue = part.ranges[category]!!
            if (op.rangeF(partValue, comparisonValue)) action else null
        }
    }

    fun Workflow.apply(part: Part): Action {
        for (rule in rules) {
            return rule.apply(part) ?: continue
        }
        return defaultAction
    }

    fun Workflows.apply(part: Part): TerminalAction {
        var workflow = this["in"] ?: wtf("No workflow named `in`")
        while (true) {
            when (val action = workflow.apply(part)) {
                is TerminalAction -> return action
                is Jump -> workflow = this[action.workflow] ?: wtf("Unknown workflow ${action.workflow}")
            }
        }
    }

    part1(check = 19114L, ::parseWorkflowAndParts) { (workflows, parts) ->
        fun ListedPart.totalRating() = values.values.sum()

        parts.filter { workflows.apply(it) == Accept }.sumOf {
            it.totalRating()
        }
    }

    part2(check = 167409079868000L, ::parseWorkflowAndParts) { (workflows) ->
        val allRules = workflows.values.flatMap { it.rules }
        val categoryRanges = Category.entries.associateWith { cat ->
            allRules.filter { it.category == cat }
                .flatMapTo(sortedSetOf(1, 4001)) {
                    when (it.op) {
                        GreaterThan -> listOf(it.comparisonValue + 1)
                        LessThan -> listOf(it.comparisonValue)
                    }
                }
                .zipWithNext { start, next -> start..<next }
        }.println()

        val partRanges =
            categoryRanges[Category.x]!!.asSequence().flatMap { xRange ->
                categoryRanges[Category.m]!!.asSequence().flatMap { mRange ->
                    categoryRanges[Category.a]!!.asSequence().flatMap { aRange ->
                        categoryRanges[Category.s]!!.asSequence().map { sRange ->
                            PartRange(
                                mapOf(
                                    Category.x to xRange,
                                    Category.m to mRange,
                                    Category.a to aRange,
                                    Category.s to sRange,
                                )
                            )
                        }
                    }
                }
            }

        val totalCombination = categoryRanges.values.productOf { it.size.toLong() }

        fun PartRange.totalRating() = ranges.values.productOf { it.span() }

        var count = 0L
        partRanges.onEach {
            if (count++ % 1000000 == 0L) {
                println("${count.toFloat() / totalCombination * 100}% ($count/$totalCombination)")
            }
        }.filter { workflows.apply(it) == Accept }.sumOf {
            it.totalRating()
        }
    }
}