package com.dbflow5.ksp

import com.dbflow5.codegen.shared.sharedModule
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.grosner.dbflow5.codegen.kotlin.codeGenModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

/**
 * Description:
 */
class DBFlowSymbolProcessorProvider : SymbolProcessorProvider, KoinComponent {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        startKoin {
            modules(
                sharedModule,
                codeGenModule,
                getModule(environment),
            )
        }
        return inject<DBFlowKspProcessor>().value
    }
}