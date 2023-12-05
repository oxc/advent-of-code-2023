import kotlinx.coroutines.*
import java.util.concurrent.Executors

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

fun runParallel(block: suspend CoroutineScope.() -> Unit): Unit =
    runBlocking(Executors.newFixedThreadPool(8).asCoroutineDispatcher(), block)