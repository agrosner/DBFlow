package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.converter.TypeConverter

/**
 * Description:
 */
actual abstract class DatabaseHolder : InternalDatabaseHolder() {

    fun putTypeConverterForClass(clazz: Class<*>, typeConverter: TypeConverter<*, *>) {
        typeConverters[clazz.kotlin] = typeConverter
    }

    fun getTypeConverterForClass(clazz: Class<*>): TypeConverter<*, *>? = getTypeConverterForClass(clazz.kotlin)
}