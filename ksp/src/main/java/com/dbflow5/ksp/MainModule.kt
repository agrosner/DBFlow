package com.dbflow5.ksp

import com.dbflow5.ksp.parser.DatabasePropertyParser
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.QueryPropertyParser
import com.dbflow5.ksp.parser.TablePropertyParser
import com.dbflow5.ksp.parser.ViewPropertyParser
import org.koin.dsl.module

/**
 * Description:
 */
val module = module {
    single { KSPropertyDeclarationParser() }
    single { DatabasePropertyParser() }
    single { DBFlowKspProcessor(get()) }
    single { TablePropertyParser() }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { KSClassDeclarationParser(get(), get(), get(), get(), get()) }
}
