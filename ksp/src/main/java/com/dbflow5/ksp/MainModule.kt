package com.dbflow5.ksp

import com.dbflow5.ksp.parser.DatabasePropertyParser
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import org.koin.dsl.module

/**
 * Description:
 */
val module = module {
    single { KSPropertyDeclarationParser() }
    single { KSClassDeclarationParser(get(), get()) }
    single { DatabasePropertyParser() }
    single { DBFlowKspProcessor(get()) }
}
