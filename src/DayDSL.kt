import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.system.exitProcess

fun <Out> day(day: Int, body: DayBuilder<Out>.() -> Unit) {
    val builder = DayBuilder<Out>(day)
    builder.body()
    builder()
}

typealias Input = List<String>

/**
 * Reads lines from the given input txt file.
 */
fun readInput(dayName: String, suffix: String = "") =
    Path("src/${dayName.lowercase()}/$dayName$suffix.txt").takeIf { it.exists() }?.readLines()

class Part<Out>(val dayName: String, val name: String, val checks: Map<String, Out>?, val body: (Input) -> Out) {
    fun run(input: List<String>) {
        println("\n\n")
        val header = "Running $name"
        println(header)
        println("─".repeat(header.length))
        val result = body(input)
        val message = "$name: $result"
        println("╭${"─".repeat(message.length + 4)}╮")
        println("│  $message  │")
        println("╰${"─".repeat(message.length + 4)}╯")
    }

    fun runChecks() {
        if (checks == null) {
            println("WARNING: No test check for $name")
            exitProcess(1)
        }

        checks.forEach { (testFile, check) ->
            val input = readInput(dayName, "_$testFile") ?: run {
                println("WARNING: No test input for $name $testFile")
                exitProcess(1)
            }
            println("\n\nDoing $name test run '$testFile'")
            val result = body(input)
            if (result == check) {
                println("[✔] $name test '$testFile' passed ($result)")
            } else {
                println("[✘] $name failed test '$testFile'")
                println("Expected: $check")
                println("Actual:   $result")
                exitProcess(1)
            }
        }
    }
}

class DayBuilder<Out>(day: Int) {
    val name = "Day${day.toString().padStart(2, '0')}"

    inner class PartBuilder(val partName: String) {
        var part: Part<Out>? = null

        operator fun invoke(checks: Map<String, Out>?, body: (Input) -> Out) {
            part = Part(name, "$name $partName", checks, body)
        }

        operator fun invoke(check: Out? = null, body: (Input) -> Out) {
            this(checks = check?.let { mapOf("test" to it) }, body)
        }

        operator fun <In> invoke(checks: Map<String, Out>? = null, parser: (Input) -> In, body: (In) -> Out) {
            this(checks = checks) { input -> body(parser(input)) }
        }

        operator fun <In> invoke(check: Out?, parser: (Input) -> In, body: (In) -> Out) {
            this(checks = check?.let { mapOf("test" to it) }, parser, body)
        }
    }

    val part1 = PartBuilder("Part 1")
    val part2 = PartBuilder("Part 2")

    operator fun invoke() {
        val part1 = this.part1.part
        val part2 = this.part2.part

        part1?.runChecks()
        part2?.runChecks()

        val input = readInput(name) ?: run {
            println("ERROR: No input for $name defined")
            exitProcess(1)
        }
        println("Day $name")
        part1?.run(input)
        part2?.run(input)
    }
}