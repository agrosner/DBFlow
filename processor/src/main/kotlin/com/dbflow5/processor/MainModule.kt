package com.dbflow5.processor

import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.processor.interop.KaptFieldSanitizer
import com.dbflow5.processor.interop.KaptOriginatingFileTypeSpecAdder
import com.dbflow5.processor.parser.ColumnMapParser
import com.dbflow5.processor.parser.ColumnMapReferencePropertyParser
import com.dbflow5.processor.parser.DatabasePropertyParser
import com.dbflow5.processor.parser.FieldPropertyParser
import com.dbflow5.processor.parser.ForeignKeyParser
import com.dbflow5.processor.parser.ForeignKeyReferencePropertyParser
import com.dbflow5.processor.parser.Fts4Parser
import com.dbflow5.processor.parser.IndexGroupParser
import com.dbflow5.processor.parser.IndexParser
import com.dbflow5.processor.parser.KaptElementProcessor
import com.dbflow5.processor.parser.KaptPropertyElementParser
import com.dbflow5.processor.parser.ManyToManyParser
import com.dbflow5.processor.parser.ManyToManyPropertyParser
import com.dbflow5.processor.parser.MigrationParser
import com.dbflow5.processor.parser.MultipleManyToManyParser
import com.dbflow5.processor.parser.NotNullPropertyParser
import com.dbflow5.processor.parser.OneToManyPropertyParser
import com.dbflow5.processor.parser.QueryPropertyParser
import com.dbflow5.processor.parser.TablePropertyParser
import com.dbflow5.processor.parser.TypeConverterPropertyParser
import com.dbflow5.processor.parser.UniqueGroupPropertyParser
import com.dbflow5.processor.parser.UniquePropertyParser
import com.dbflow5.processor.parser.ViewPropertyParser
import org.koin.dsl.module
import javax.annotation.processing.ProcessingEnvironment

/**
 * Description:
 */
fun getModule(
    env: ProcessingEnvironment,
) = module {
    single { DatabasePropertyParser() }
    single { FieldPropertyParser() }
    single { ForeignKeyParser(get()) }
    single { ColumnMapParser(get()) }
    single { ForeignKeyReferencePropertyParser() }
    single { Fts4Parser() }
    single { IndexGroupParser() }
    single { IndexParser() }
    single { ManyToManyPropertyParser() }
    single { MigrationParser() }
    single { NotNullPropertyParser() }
    single { OneToManyPropertyParser() }
    single { QueryPropertyParser() }
    single { TablePropertyParser(get(), get()) }
    single { TypeConverterPropertyParser() }
    single { UniqueGroupPropertyParser() }
    single { UniquePropertyParser() }
    single { ViewPropertyParser() }
    single<FieldSanitizer> {
        KaptFieldSanitizer(
            get(), get(),
            get()
        )
    }
    single { ColumnMapReferencePropertyParser() }
    single {
        KaptPropertyElementParser(
            get(),
            get(), get(),
            get(), get(),
            get(),
        )
    }
    single {
        KaptElementProcessor(
            env.elementUtils,
            get(), get(), get(),
            get(), get(), get(),
            get(), get(), get(),
            get(), get(),
        )
    }
    single { ManyToManyParser(get()) }
    single { MultipleManyToManyParser(get()) }
    single {
        DBFlowKaptProcessor(
            env.elementUtils,
            env.filer,
            env.messager,
            get(),
            get(),
        )
    }

    single<OriginatingFileTypeSpecAdder> { KaptOriginatingFileTypeSpecAdder() }
}
