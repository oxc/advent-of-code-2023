package day20

import Input
import day
import wtf

typealias Modules = Map<String, Module>

enum class Pulse { high, low }

sealed class Module(val name: String, val targetNames: List<String>) {
    lateinit var targets: List<Module>
    private val _inputs: MutableSet<Module> = mutableSetOf()
    protected val inputs: Set<Module> = _inputs

    fun init(modules: Modules) {
        targets =
            targetNames.map { modules[it]?.also { module -> module._inputs.add(this) } ?: wtf("No module named $it") }
    }

    abstract fun pulse(from: Module, pulse: Pulse): Pulse?
}

class Broadcaster(targetNames: List<String>) : Module("broadcaster", targetNames) {
    override fun pulse(from: Module, pulse: Pulse) = pulse
}

class FlipFlop(name: String, targetNames: List<String>) : Module(name, targetNames) {
    var on = false

    override fun pulse(from: Module, pulse: Pulse) = when (pulse) {
        Pulse.high -> null
        Pulse.low -> {
            on = !on
            when (on) {
                true -> Pulse.high
                false -> Pulse.low
            }
        }
    }
}

class Conjunction(name: String, targetNames: List<String>) : Module(name, targetNames) {
    private val lastInput by lazy { inputs.associateWithTo(mutableMapOf()) { Pulse.low } }

    override fun pulse(from: Module, pulse: Pulse): Pulse? {
        lastInput[from] = pulse
        return if (lastInput.values.all { it == Pulse.high }) Pulse.low else Pulse.high
    }
}

class Untyped(name: String) : Module(name, emptyList()) {
    override fun pulse(from: Module, pulse: Pulse) = null
}

data class TargetedPulse(val from: Module, val to: Module, val pulse: Pulse)

fun main() = day(20) {

    fun parseModules(input: Input): Modules = input.map { line ->
        val (def, sTargetNames) = line.split(" -> ")
        val targetNames = sTargetNames.split(", ")
        when (def.first()) {
            '%' -> FlipFlop(def.substring(1), targetNames)
            '&' -> Conjunction(def.substring(1), targetNames)
            'b' -> Broadcaster(targetNames)
            else -> wtf("Unknown module: $def")
        }
    }.associateByTo(mutableMapOf()) { it.name }.also { modules ->
        modules.values.flatMapTo(mutableSetOf()) { it.targetNames }.minus(modules.keys).forEach { name ->
            modules[name] = Untyped(name)
        }
        modules.values.forEach { it.init(modules) }
    }

    part1(checks = mapOf("test" to 32000000L, "test2" to 11687500L), ::parseModules) { modules ->
        val broadcaster = modules.values.first { it is Broadcaster }

        var lowCount = 0L
        var highCount = 0L

        val pendingPulses = ArrayDeque<TargetedPulse>()
        fun sendPulse(from: Module, to: Module, pulse: Pulse) {
            when (pulse) {
                Pulse.high -> highCount += 1
                Pulse.low -> lowCount += 1
            }
            pendingPulses += TargetedPulse(from, to, pulse)
        }

        repeat(1000) {
            sendPulse(from = broadcaster, to = broadcaster, Pulse.low)
            while (pendingPulses.isNotEmpty()) {
                val (from, module, pulse) = pendingPulses.removeFirst()
                val output = module.pulse(from, pulse) ?: continue
                module.targets.forEach { target ->
                    sendPulse(module, target, output)
                }
            }
        }

        highCount * lowCount
    }

}