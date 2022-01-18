package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import org.koin.dsl.module

val sharedModule = module {
    single { ReferencesCache(get()) }
    single { TypeConverterCache() }
    single { SQLiteLookup() }
}
