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

class Part<Out>(val name: String, val check: Out? = null, val body: (Input) -> Out) {
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

    fun runCheck(input: Input?) {
        if (input == null) {
            println("WARNING: No test input for $name")
            exitProcess(1)
        }
        if (check == null) {
            println("WARNING: No test check for $name")
            exitProcess(1)
        }
        println("\n\nDoing $name test run")
        val result = body(input)
        if (result == check) {
            println("[✔] $name test passed ($result)")
        } else {
            println("[✘] $name failed test")
            println("Expected: $check")
            println("Actual:   $result")
            exitProcess(1)
        }
    }
}

class DayBuilder<Out>(day: Int) {
    val name = "Day${day.toString().padStart(2, '0')}"

    private var part1: Part<Out>? = null
    private var part2: Part<Out>? = null

    fun part1(check: Out? = null, body: (Input) -> Out) {
        part1 = Part("$name Part 1", check = check, body)
    }

    fun <In> part1(check: Out? = null, parser: (Input) -> In, body: (In) -> Out) {
        part1(check = check) { input -> body(parser(input)) }
    }

    fun part2(check: Out? = null, body: (Input) -> Out) {
        part2 = Part("$name Part 2", check = check, body)
    }

    fun <In> part2(check: Out? = null, parser: (Input) -> In, body: (In) -> Out) {
        part2(check = check) { input -> body(parser(input)) }
    }

    operator fun invoke() {
        if (part1?.check != null || part2?.check != null) {
            val test1 = Path("src/${name}_test.txt").takeIf { it.exists() }?.readLines()
            part1?.runCheck(test1)

            val test2 = Path("src/${name}_test2.txt").takeIf { it.exists() }?.readLines() ?: test1
            part2?.runCheck(test2)
        }

        val input = readInput(name)
        println("Day $name")
        part1?.run(input)
        part2?.run(input)
    }
}