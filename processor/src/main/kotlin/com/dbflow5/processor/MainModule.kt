package com.dbflow5.processor

import com.dbflow5.codegen.model.cache.TypeConverterCache
import com.dbflow5.codegen.parser.FieldSanitizer
import com.dbflow5.processor.parser.ColumnMapParser
import com.dbflow5.processor.parser.DatabasePropertyParser
import com.dbflow5.processor.parser.FieldPropertyParser
import com.dbflow5.processor.parser.ForeignKeyParser
import com.dbflow5.processor.parser.ForeignKeyReferencePropertyParser
import com.dbflow5.processor.parser.Fts4Parser
import com.dbflow5.processor.parser.IndexGroupParser
import com.dbflow5.processor.parser.IndexParser
import com.dbflow5.processor.parser.KaptElementProcessor
import com.dbflow5.processor.parser.ManyToManyPropertyParser
import com.dbflow5.processor.parser.MigrationParser
import com.dbflow5.processor.parser.NotNullPropertyParser
import com.dbflow5.processor.parser.OneToManyPropertyParser
import com.dbflow5.processor.parser.QueryPropertyParser
import com.dbflow5.processor.parser.TablePropertyParser
import com.dbflow5.processor.parser.TypeConverterPropertyParser
import com.dbflow5.processor.parser.UniqueGroupPropertyParser
import com.dbflow5.processor.parser.UniquePropertyParser
import com.dbflow5.processor.parser.ViewPropertyParser
import org.koin.dsl.module
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements

/**
 * Description:
 */
fun getModule(
    elements: Elements,
    messager: Messager,
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
    single { TypeConverterCache() }
    single { KaptFieldSanitizer() }
    single {
        KaptElementProcessor(
            elements,
            env,
            get(), get(), get(),
            get(), get(), get(),
            get()
        )
    }

    single {
        DBFlowKaptProcessor(elements, get(), messager, get())
    }
}
