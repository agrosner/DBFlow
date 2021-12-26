package com.dbflow5.ksp

import com.dbflow5.ksp.model.SQLiteLookup
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.parser.*
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.dbflow5.ksp.writer.*
import com.dbflow5.ksp.writer.classwriter.*
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import org.koin.dsl.module

/**
 * Description:
 */
fun getModule(environment: SymbolProcessorEnvironment) = module {
    single { KSPropertyDeclarationParser(get(), get()) }
    single { DatabasePropertyParser() }
    single { SQLiteLookup() }
    single {
        DBFlowKspProcessor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            environment,
            get(),
            get(),
        )
    }
    single { TypeConverterPropertyParser() }
    single { TablePropertyParser() }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { ReferenceHolderProperyParser(get()) }
    single { ManyToManyPropertyParser() }
    single { ForeignKeyReferencePropertyParser() }
    single { FieldSanitizer(get(), get()) }
    single { KSClassDeclarationParser(get(), get(), get(), get(), get(), get(), get()) }

    single { LoadFromCursorWriter(get(), get()) }
    single { GetPropertyMethodWriter(get()) }
    single { AllColumnPropertiesWriter(get()) }
    single { PrimaryConditionClauseWriter(get()) }
    single { StatementBinderWriter(get(), get()) }
    single { TypeConverterFieldWriter() }
    single { InlineTypeConverterWriter() }
    single { ManyToManyClassWriter() }
    single { CreationQueryWriter(get(), get()) }

    single {
        ClassWriter(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(),
        )
    }
    single { DatabaseWriter() }
    single { FieldPropertyWriter(get()) }
    single { DatabaseHolderWriter() }
    single { ReferencesCache(get()) }
    single { TypeConverterCache() }
}
