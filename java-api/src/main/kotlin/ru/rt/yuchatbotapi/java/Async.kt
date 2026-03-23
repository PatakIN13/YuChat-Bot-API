package ru.rt.yuchatbotapi.java

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

/**
 * Scope для запуска корутин из Java-обёрток.
 * Используется общий пул потоков (Dispatchers.IO).
 */
internal val javaScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** Запускает suspend-блок и возвращает CompletableFuture. */
internal fun <T> async(block: suspend CoroutineScope.() -> T): CompletableFuture<T> =
    javaScope.future(block = block)

/** Запускает suspend-блок без возвращаемого значения. */
internal fun asyncVoid(block: suspend CoroutineScope.() -> Unit): CompletableFuture<Void> =
    javaScope.future { block(); @Suppress("UNCHECKED_CAST") (null as Void?) }.thenApply { null }
