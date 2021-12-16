package com.dbflow5.ksp.model.cache

import com.dbflow5.ksp.model.TypeConverterModel
import com.squareup.kotlinpoet.TypeName

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache {

    private var typeConverters = mutableMapOf<TypeName, TypeConverterModel>()

    fun putTypeConverter(typeConverterModel: TypeConverterModel) {
        typeConverters[typeConverterModel.modelClassType] = typeConverterModel
    }

    operator fun get(typeName: TypeName) = typeConverters.getValue(typeName)
}


