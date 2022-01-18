package com.dbflow5.ksp

import com.dbflow5.codegen.model.SQLiteLookup
import com.dbflow5.codegen.model.cache.ReferencesCache
import com.dbflow5.codegen.model.cache.TypeConverterCache
import com.dbflow5.codegen.model.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.parser.FieldSanitizer
import com.dbflow5.ksp.model.interop.KSPOriginatingFileTypeSpecAdder
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPFieldSanitizer
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
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.grosner.dbflow5.codegen.kotlin.writer.ClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseHolderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.InlineTypeConverterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ManyToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.OneToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.AllColumnPropertiesWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.CreationQueryWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.FieldPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.GetPropertyMethodWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.IndexPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.LoadFromCursorWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.PrimaryConditionClauseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.StatementBinderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.TypeConverterFieldWriter
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
    single<FieldSanitizer> { KSPFieldSanitizer(get(), get()) }
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
    single { InlineTypeConverterWriter(get()) }
    single { ManyToManyClassWriter(get()) }
    single { OneToManyClassWriter(get()) }
    single { CreationQueryWriter(get(), get(), get()) }
    single { IndexPropertyWriter(get()) }

    single {
        ClassWriter(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(),
            get(),
        )
    }
    single { DatabaseWriter(get()) }
    single { FieldPropertyWriter(get()) }
    single { DatabaseHolderWriter(get()) }
    single { ReferencesCache(get()) }
    single { TypeConverterCache() }
    single<OriginatingFileTypeSpecAdder> {
        KSPOriginatingFileTypeSpecAdder()
    }
}
