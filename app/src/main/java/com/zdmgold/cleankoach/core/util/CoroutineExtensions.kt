package com.zdmgold.cleankoach.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun CoroutineScope.launchLogged(
    block: suspend CoroutineScope.() -> Unit
): Job = launch(block = block)

fun tickerFlow(periodMillis: Long, initialDelayMillis: Long = 0L): Flow<Unit> = flow {
    delay(initialDelayMillis)
    while (true) {
        emit(Unit)
        delay(periodMillis)
    }
}
