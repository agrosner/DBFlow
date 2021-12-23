package com.dbflow5.ksp

import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.parser.*
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseHolderWriter
import com.dbflow5.ksp.writer.DatabaseWriter
import com.dbflow5.ksp.writer.classwriter.*
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import org.koin.dsl.module

/**
 * Description:
 */
fun getModule(environment: SymbolProcessorEnvironment) = module {
    single { KSPropertyDeclarationParser(get(), get()) }
    single { DatabasePropertyParser() }
    single {
        DBFlowKspProcessor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            environment,
        )
    }
    single { TypeConverterPropertyParser() }
    single { TablePropertyParser() }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { ReferenceHolderProperyParser(get()) }
    single { ForeignKeyReferencePropertyParser() }
    single { FieldSanitizer(get()) }
    single { KSClassDeclarationParser(get(), get(), get(), get(), get(), get()) }

    single { LoadFromCursorWriter(get(), get()) }
    single { GetPropertyMethodWriter(get()) }
    single { AllColumnPropertiesWriter(get()) }
    single { PrimaryConditionClauseWriter(get()) }
    single { StatementBinderWriter(get(), get()) }
    single { TypeConverterFieldWriter() }

    single {
        ClassWriter(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get()
        )
    }
    single { DatabaseWriter() }
    single { FieldPropertyWriter(get()) }
    single { DatabaseHolderWriter() }
    single { ReferencesCache(get()) }
    single { TypeConverterCache(environment.logger) }
}
