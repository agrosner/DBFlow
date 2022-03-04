package com.dbflow5.ksp

import com.dbflow5.codegen.shared.Platforms
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.ksp.model.interop.KSPOriginatingFileTypeSpecAdder
import com.dbflow5.ksp.model.interop.KSPPlatforms
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
import org.koin.dsl.module

/**
 * Description:
 */
fun getModule(environment: SymbolProcessorEnvironment) = module {
    single { KSPropertyDeclarationParser(get(), get(), get(), get(), get()) }
    single { DatabasePropertyParser() }
    single {
        DBFlowKspProcessor(
            get(),
            environment,
            get(),
            get(),
        )
    }
    single<Platforms> { KSPPlatforms(environment) }
    single { TypeConverterPropertyParser() }
    single { TablePropertyParser(get(), get()) }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { ReferenceHolderPropertyParser(get()) }
    single { ManyToManyPropertyParser() }
    single { OneToManyPropertyParser() }
    single { ForeignKeyReferencePropertyParser() }
    single<FieldSanitizer> {
        KSPFieldSanitizer(
            get(), get(),
        )
    }
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

    single<OriginatingFileTypeSpecAdder> {
        KSPOriginatingFileTypeSpecAdder()
    }
}
