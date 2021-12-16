package com.dbflow5.ksp.model.cache

import com.dbflow5.converter.*
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache(
    private val classDeclarationParser: KSClassDeclarationParser,
) {

    private var typeConverters = mutableMapOf<TypeName, TypeConverterModel>()

    fun applyResolver(resolver: Resolver) {
        DEFAULT_TYPE_CONVERTERS.forEach { defaultType ->
            val typeName = defaultType.asClassName()
            val declaration =
                resolver.getClassDeclarationByName(resolver.getKSNameFromString(typeName.toString()))!!
            val classModel = classDeclarationParser.parse(declaration) as TypeConverterModel
            putTypeConverter(classModel)
        }
    }


    fun putTypeConverter(typeConverterModel: TypeConverterModel) {
        typeConverters[typeConverterModel.modelClassType] = typeConverterModel
    }

    operator fun get(typeName: TypeName) = typeConverters.getValue(typeName)

    companion object {
        private val DEFAULT_TYPE_CONVERTERS = arrayOf(
            CalendarConverter::class,
            BigDecimalConverter::class,
            BigIntegerConverter::class,
            DateConverter::class,
            SqlDateConverter::class,
            BooleanConverter::class,
            UUIDConverter::class,
            CharConverter::class
        )
    }
}

