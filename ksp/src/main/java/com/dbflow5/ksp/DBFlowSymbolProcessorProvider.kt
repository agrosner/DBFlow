package com.dbflow5.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

/**
 * Description:
 */
class DBFlowSymbolProcessorProvider : SymbolProcessorProvider, KoinComponent {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        startKoin {
            modules(getModule(environment))
        }
        return inject<DBFlowKspProcessor>().value
    }
}