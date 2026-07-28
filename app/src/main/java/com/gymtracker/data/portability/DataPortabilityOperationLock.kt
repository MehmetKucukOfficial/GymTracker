package com.gymtracker.data.portability

import kotlinx.coroutines.sync.Mutex

internal object DataPortabilityOperationLock {
    val mutex = Mutex()
}
