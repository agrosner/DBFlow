package com.dbflow5.ksp

import com.dbflow5.ksp.model.ReferencesCache
import com.dbflow5.ksp.parser.DatabasePropertyParser
import com.dbflow5.ksp.parser.FieldPropertyParser
import com.dbflow5.ksp.parser.ForeignKeyPropertyParser
import com.dbflow5.ksp.parser.ForeignKeyReferencePropertyParser
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.QueryPropertyParser
import com.dbflow5.ksp.parser.TablePropertyParser
import com.dbflow5.ksp.parser.ViewPropertyParser
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseHolderWriter
import com.dbflow5.ksp.writer.DatabaseWriter
import com.dbflow5.ksp.writer.FieldPropertyWriter
import com.dbflow5.ksp.writer.PropertyStatementWrapperWriter
import org.koin.dsl.module

/**
 * Description:
 */
val module = module {
    single { KSPropertyDeclarationParser(get(), get()) }
    single { DatabasePropertyParser() }
    single {
        DBFlowKspProcessor(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single { TablePropertyParser() }
    single { QueryPropertyParser() }
    single { ViewPropertyParser() }
    single { FieldPropertyParser() }
    single { ForeignKeyPropertyParser(get()) }
    single { ForeignKeyReferencePropertyParser() }
    single { KSClassDeclarationParser(get(), get(), get(), get(), get()) }
    single { ClassWriter(get(), get(), get()) }
    single { DatabaseWriter() }
    single { FieldPropertyWriter() }
    single { DatabaseHolderWriter() }
    single { PropertyStatementWrapperWriter() }
    single { ReferencesCache() }
}
