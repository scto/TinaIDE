package com.scto.mobileide.ai.tools

import kotlinx.coroutines.CancellationException

internal fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) {
        throw this
    }
}
