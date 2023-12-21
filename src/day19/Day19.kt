package day19

import Input
import day
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

sealed class Operator(val f: (Long, Long) -> Boolean)
data object GreaterThan : Operator({ a, b -> a > b })
data object LessThan : Operator({ a, b -> a < b })


enum class Category { x, m, a, s }

data class Rule(val category: Category, val op: Operator, val comparisonValue: Long, val action: Action)
data class Workflow(val id: String, val rules: List<Rule>, val defaultAction: Action)

typealias Workflows = SortedMap<String, Workflow>

typealias Part = Map<Category, Long>

typealias PartsRange = Map<Category, LongRange>

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
        }.toMap()
    }

    fun parseWorkflowAndParts(input: Input): Pair<Workflows, List<Part>> {
        val (inWorkflow, inParts) = input.split { it.isBlank() }
        val workflows = parseWorkflows(inWorkflow)
        val parts = parseParts(inParts)
        return workflows to parts
    }

    fun Rule.apply(part: Part): Action? {
        val partValue = part[category] ?: 0L
        return if (op.f(partValue, comparisonValue)) action else null
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

    fun Part.totalRating() = values.sum()

    part1(check = 19114L, ::parseWorkflowAndParts) { (workflows, parts) ->
        parts.filter { workflows.apply(it) == Accept }.sumOf {
            it.totalRating()
        }
    }

    data class CountingStep(val workflowId: String, val ranges: PartsRange)

    fun PartsRange.copy(category: Category, range: LongRange): PartsRange =
        this.toMutableMap().also { it[category] = range }

    fun Rule.split(parts: PartsRange): Pair<PartsRange?, PartsRange?> {
        val operand = parts[category]!!
        return when (op) {
            GreaterThan -> when {
                operand.first > comparisonValue -> parts to null
                operand.last <= comparisonValue -> null to parts
                else -> parts.copy(category, comparisonValue + 1..operand.last) to
                        parts.copy(category, operand.first..comparisonValue)
            }

            LessThan -> when {
                operand.last < comparisonValue -> parts to null
                operand.first >= comparisonValue -> null to parts
                else -> parts.copy(category, operand.first..<comparisonValue) to
                        parts.copy(category, comparisonValue..operand.last)
            }
        }
    }

    fun PartsRange.countCombinations() = values.productOf { it.span() }

    fun Workflows.countCombinations(): Long {
        val steps = ArrayDeque(listOf(CountingStep("in", Category.entries.associateWith { 1L..4000L })))
        var count = 0L
        step@ while (steps.isNotEmpty()) {
            val step = steps.removeFirst()
            val workflow = this[step.workflowId] ?: wtf("No workflow")
            var current = step.ranges
            for (rule in workflow.rules) {
                val (applies, next) = rule.split(current)
                applies?.let {
                    when (rule.action) {
                        Accept -> count += it.countCombinations()
                        Reject -> {}
                        is Jump -> steps += CountingStep(rule.action.workflow, it)
                    }
                }
                current = next ?: continue@step
            }
            when (workflow.defaultAction) {
                Accept -> count += current.countCombinations()
                Reject -> {}
                is Jump -> steps += CountingStep(workflow.defaultAction.workflow, current)
            }
        }
        return count
    }

    part2(check = 167409079868000L, ::parseWorkflowAndParts) { (workflows) ->
        workflows.countCombinations()
    }
}