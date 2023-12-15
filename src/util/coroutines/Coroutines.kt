package util.coroutines

import kotlinx.coroutines.*
import java.util.concurrent.Executors

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

fun <T> runParallel(block: suspend CoroutineScope.() -> T): T =
    runBlocking(Executors.newFixedThreadPool(8).asCoroutineDispatcher(), block)