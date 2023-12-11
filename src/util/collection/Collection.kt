package util.collection

inline fun <T> Iterable<T>.productOf(selector: (T) -> Long): Long = fold(1L) { acc, i -> acc * selector(i) }
fun <T> Sequence<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }
fun <T> Iterable<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }