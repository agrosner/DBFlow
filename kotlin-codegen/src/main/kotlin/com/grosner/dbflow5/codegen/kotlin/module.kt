package com.grosner.dbflow5.codegen.kotlin

import com.grosner.dbflow5.codegen.kotlin.writer.AutoIncrementUpdateWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ClassAdapterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.CreationSQLWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseHolderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.InlineTypeConverterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ManyToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ObjectWriter
import com.grosner.dbflow5.codegen.kotlin.writer.OneToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.PrimaryModelClauseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.PropertyGetterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.QueryOpsWriter
import com.grosner.dbflow5.codegen.kotlin.writer.TableBinderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.TableOpsWriter
import com.grosner.dbflow5.codegen.kotlin.writer.TableSQLWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.AllColumnPropertiesWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.CreationQueryWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.FieldPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.GetPropertyMethodWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.IndexPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.LoadFromCursorWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.PrimaryConditionClauseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.StatementBinderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.TypeConverterFieldWriter
import com.squareup.kotlinpoet.NameAllocator
import org.koin.dsl.module

/**
 * Description:
 */
val codeGenModule = module {
    single { NameAllocator() }
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
            get(), get(), get(),
        )
    }
    single { DatabaseWriter(get()) }
    single { FieldPropertyWriter(get()) }
    single { DatabaseHolderWriter(get(), get(), get()) }
    single { TableSQLWriter(get(), get()) }
    single { TableBinderWriter(get(), get(), get()) }
    single { PrimaryModelClauseWriter(get(), get()) }
    single { AutoIncrementUpdateWriter(get()) }
    single { TableOpsWriter(get(), get()) }
    single { ClassAdapterWriter(get(), get(), get()) }
    single { PropertyGetterWriter(get(), get()) }
    single { QueryOpsWriter(get(), get(), get()) }
    single { CreationSQLWriter(get(), get(), get(), get()) }

    single {
        ObjectWriter(
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
}