package com.dbflow5.ksp

import com.dbflow5.model.SQLiteLookup
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.annotation.DatabasePropertyParser
import com.dbflow5.ksp.parser.annotation.FieldPropertyParser
import com.dbflow5.ksp.parser.annotation.ForeignKeyReferencePropertyParser
import com.dbflow5.ksp.parser.annotation.Fts4Parser
import com.dbflow5.ksp.parser.annotation.IndexGroupParser
import com.dbflow5.ksp.parser.annotation.IndexParser
import com.dbflow5.ksp.parser.annotation.ManyToManyParser
import com.dbflow5.ksp.parser.annotation.ManyToManyPropertyParser
import com.dbflow5.ksp.parser.annotation.MigrationParser
import com.dbflow5.ksp.parser.annotation.MultipleManyToManyParser
import com.dbflow5.ksp.parser.annotation.NotNullPropertyParser
import com.dbflow5.ksp.parser.annotation.OneToManyPropertyParser
import com.dbflow5.ksp.parser.annotation.QueryPropertyParser
import com.dbflow5.ksp.parser.annotation.ReferenceHolderPropertyParser
import com.dbflow5.ksp.parser.annotation.TablePropertyParser
import com.dbflow5.ksp.parser.annotation.TypeConverterPropertyParser
import com.dbflow5.ksp.parser.annotation.UniqueGroupPropertyParser
import com.dbflow5.ksp.parser.annotation.UniquePropertyParser
import com.dbflow5.ksp.parser.annotation.ViewPropertyParser
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseHolderWriter
import com.dbflow5.ksp.writer.DatabaseWriter
import com.dbflow5.ksp.writer.InlineTypeConverterWriter
import com.dbflow5.ksp.writer.ManyToManyClassWriter
import com.dbflow5.ksp.writer.OneToManyClassWriter
import com.dbflow5.ksp.writer.classwriter.AllColumnPropertiesWriter
import com.dbflow5.ksp.writer.classwriter.CreationQueryWriter
import com.dbflow5.ksp.writer.classwriter.FieldPropertyWriter
import com.dbflow5.ksp.writer.classwriter.GetPropertyMethodWriter
import com.dbflow5.ksp.writer.classwriter.IndexPropertyWriter
import com.dbflow5.ksp.writer.classwriter.LoadFromCursorWriter
import com.dbflow5.ksp.writer.classwriter.PrimaryConditionClauseWriter
import com.dbflow5.ksp.writer.classwriter.StatementBinderWriter
import com.dbflow5.ksp.writer.classwriter.TypeConverterFieldWriter
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import org.koin.dsl.module

/**
 * Description:
 */
fun getModule(environment: SymbolProcessorEnvironment) = module {
    single { KSPropertyDeclarationParser(get(), get(), get(), get(), get()) }
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
            get(),
        )
    }
    single { TypeConverterPropertyParser() }
    single { TablePropertyParser(get(), get()) }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { ReferenceHolderPropertyParser(get()) }
    single { ManyToManyPropertyParser() }
    single { OneToManyPropertyParser() }
    single { ForeignKeyReferencePropertyParser() }
    single { FieldSanitizer(get(), get()) }
    single {
        KSClassDeclarationParser(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single { Fts4Parser() }
    single { IndexParser() }
    single { IndexGroupParser() }
    single { NotNullPropertyParser() }
    single { UniqueGroupPropertyParser() }
    single { UniquePropertyParser() }
    single { MigrationParser() }
    single { ManyToManyParser(get()) }
    single { MultipleManyToManyParser(get()) }

    single { LoadFromCursorWriter(get(), get()) }
    single { GetPropertyMethodWriter(get()) }
    single { AllColumnPropertiesWriter(get()) }
    single { PrimaryConditionClauseWriter(get()) }
    single { StatementBinderWriter(get(), get()) }
    single { TypeConverterFieldWriter() }
    single { InlineTypeConverterWriter() }
    single { ManyToManyClassWriter() }
    single { OneToManyClassWriter() }
    single { CreationQueryWriter(get(), get(), get()) }
    single { IndexPropertyWriter(get()) }

    single {
        ClassWriter(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(),
        )
    }
    single { DatabaseWriter() }
    single { FieldPropertyWriter(get()) }
    single { DatabaseHolderWriter() }
    single { ReferencesCache(get()) }
    single { TypeConverterCache() }
}
