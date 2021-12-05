package com.dbflow5.ksp

import com.dbflow5.ksp.parser.DatabasePropertyParser
import com.dbflow5.ksp.parser.FieldPropertyParser
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.QueryPropertyParser
import com.dbflow5.ksp.parser.TablePropertyParser
import com.dbflow5.ksp.parser.ViewPropertyParser
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.FieldPropertyWriter
import com.dbflow5.ksp.writer.PropertyStatementWrapperWriter
import org.koin.dsl.module

/**
 * Description:
 */
val module = module {
    single { KSPropertyDeclarationParser(get()) }
    single { DatabasePropertyParser() }
    single { DBFlowKspProcessor(get(), get()) }
    single { TablePropertyParser() }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { KSClassDeclarationParser(get(), get(), get(), get(), get()) }
    single { ClassWriter(get(), get()) }
    single { FieldPropertyWriter() }
    single { PropertyStatementWrapperWriter() }
}
