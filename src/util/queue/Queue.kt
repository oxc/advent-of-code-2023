package util.queue

inline fun <T> queue(initial: Collection<T>, block: ArrayDeque<T>.(head: T) -> Unit) {
    val queue = ArrayDeque(initial)
    while (queue.isNotEmpty()) {
        val head = queue.removeFirst()
        with(queue) {
            block(head)
        }
    }
}