package com.dbflow5.ksp.model.cache

import com.dbflow5.converter.*
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache {

    private var typeConverters = mutableMapOf<TypeName, TypeConverterModel>()

    fun applyResolver(resolver: Resolver) {
        DEFAULT_TYPE_CONVERTERS.forEach { defaultType ->
            val typeName = defaultType.asClassName()
            putTypeConverter(typeName, resolver)
        }
    }

    fun putTypeConverter(className: ClassName, resolver: Resolver) {
        val declaration =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(className.toString()))!!
        val typeConverterSuper = declaration.superTypes.firstNotNullOfOrNull { reference ->
            reference.resolve().toTypeName().let {
                it as? ParameterizedTypeName
            }?.takeIf { type ->
                type.rawType == ClassNames.TypeConverter
            }
        } ?: throw IllegalStateException("Error ${className}")
        val classModel = TypeConverterModel(
            name = NameModel(className),
            properties = TypeConverterProperties(listOf()),
            classType = className,
            dataClassType = typeConverterSuper.typeArguments[0],
            modelClassType = typeConverterSuper.typeArguments[1],
        )
        putTypeConverter(classModel)
    }


    fun putTypeConverter(typeConverterModel: TypeConverterModel) {
        typeConverters[typeConverterModel.modelClassType.copy(nullable = false)] =
            typeConverterModel
    }

    operator fun get(typeName: TypeName, name: String) =
        typeConverters.getOrElse(typeName.copy(nullable = false)) {
            throw IllegalStateException("Missing Key ${typeName}:${name}. Map is ${typeConverters}")
        }

    fun has(typeName: TypeName) = typeConverters.containsKey(
        typeName
            .copy(nullable = false)
    )

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

