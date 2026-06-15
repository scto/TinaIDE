package com.scto.mobileide.core.debug.di

import com.scto.mobileide.core.debug.BreakpointStore
import com.scto.mobileide.core.debug.DebugSessionStore
import org.koin.dsl.module

val debugModule = module {
    single { BreakpointStore() }
    single { DebugSessionStore() }
}
