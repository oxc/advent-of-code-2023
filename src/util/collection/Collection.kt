package util.collection

inline fun <T> Iterable<T>.productOf(selector: (T) -> Long): Long = fold(1L) { acc, i -> acc * selector(i) }
fun <T> Sequence<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }
fun <T> Iterable<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }

fun <T> Iterable<T>.eachUniquePair() = sequence {
    forEachIndexed { index, a ->
        drop(index + 1).forEach { b ->
            yield(a to b)
        }
    }
}

fun main() {
    check(listOf(1, 2, 3).eachUniquePair().toList() == listOf(1 to 2, 1 to 3, 2 to 3))
}