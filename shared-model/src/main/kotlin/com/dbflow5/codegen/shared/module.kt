package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.validation.ClassValidator
import com.dbflow5.codegen.shared.validation.FieldValidator
import com.dbflow5.codegen.shared.validation.ObjectValidatorMap
import com.dbflow5.codegen.shared.validation.TypeConverterValidator
import org.koin.dsl.module

val sharedModule = module {
    single { ReferencesCache(get()) }
    single { TypeConverterCache(get()) }
    single { SQLiteLookup() }
    single { FieldValidator() }
    single {
        ObjectValidatorMap(
            mapOf(
                ClassModel::class to ClassValidator(get()),
                TypeConverterModel.Simple::class to TypeConverterValidator(),
                TypeConverterModel.Chained::class to TypeConverterValidator(),
            ),
        )
    }
}
