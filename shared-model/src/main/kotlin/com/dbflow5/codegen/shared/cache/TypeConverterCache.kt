package com.dbflow5.codegen.shared.cache

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.properties.TypeConverterProperties
import com.dbflow5.codegen.shared.toChained
import com.dbflow5.converter.BigDecimalConverter
import com.dbflow5.converter.BigIntegerConverter
import com.dbflow5.converter.BlobConverter
import com.dbflow5.converter.BooleanConverter
import com.dbflow5.converter.CalendarConverter
import com.dbflow5.converter.CharConverter
import com.dbflow5.converter.DateConverter
import com.dbflow5.converter.SqlDateConverter
import com.dbflow5.converter.UUIDConverter
import com.dbflow5.codegen.shared.ClassNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache {

    private val typeConverters = mutableMapOf<TypeName, TypeConverterModel>()

    /**
     * Set of TypeConverters to generate
     */
    private val typeConvertersToWrite = mutableSetOf<TypeConverterModel>()
    val generatedTypeConverters: Set<TypeConverterModel> = typeConvertersToWrite

    fun applyResolver(resolver: ClassNameResolver) {
        DEFAULT_TYPE_CONVERTERS.forEach { defaultType ->
            val typeName = defaultType.asClassName()
            putTypeConverter(typeName, resolver)
        }
    }

    /**
     * Discover type converters in the map that are nested,
     * and chain them
     */
    fun processNestedConverters() {
        val reformedCache = typeConverters.map { (classType, converter) ->
            if (typeConverters.containsKey(converter.dataTypeName)) {
                var activeConverter = converter
                var chainedConverter = when (converter) {
                    is TypeConverterModel.Chained -> converter
                    is TypeConverterModel.Simple -> converter.toChained()
                }
                while (typeConverters.containsKey(activeConverter.dataTypeName)) {
                    activeConverter = typeConverters.getValue(activeConverter.dataTypeName)
                    chainedConverter = chainedConverter.append(activeConverter)
                }
                classType to chainedConverter
            } else {
                classType to converter
            }
        }
        typeConverters.clear()
        typeConverters.putAll(reformedCache)
    }

    /**
     * Add a type converter we generate on the fly - currently only used for Inline class types.
     */
    fun putGeneratedTypeConverter(typeConverterModel: TypeConverterModel) {
        putTypeConverter(typeConverterModel)
        typeConvertersToWrite.add(typeConverterModel)
    }

    fun putTypeConverter(
        className: ClassName,
        resolver: ClassNameResolver
    ) {
        val declaration = resolver.classDeclarationByClassName(className)!!
        val typeConverterSuper = extractTypeConverter(declaration, className)
        val classModel = TypeConverterModel.Simple(
            name = NameModel(className),
            properties = TypeConverterProperties(listOf()),
            classType = className,
            dataTypeName = typeConverterSuper.typeArguments[0],
            modelTypeName = typeConverterSuper.typeArguments[1],
            modelClass = declaration.asStarProjectedType(),
            originatingFile = declaration.containingFile,
        )
        putTypeConverter(classModel)
    }

    fun putTypeConverter(typeConverterModel: TypeConverterModel) {
        typeConverters[typeConverterModel.modelTypeName.copy(nullable = false)] =
            typeConverterModel
    }

    operator fun get(typeName: TypeName, name: String) =
        typeConverters.getOrElse(typeName.copy(nullable = false)) {
            throw IllegalStateException("Missing Key ${typeName}:${name}. Map is ${typeConverters}")
        }

    fun has(typeName: TypeName): Boolean = typeConverters.containsKey(
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
            CharConverter::class,
            BlobConverter::class,
        )
    }
}

fun extractTypeConverter(
    declaration: ClassDeclaration,
    className: ClassName
): ParameterizedTypeName {
    val typeConverterSuper = declaration.superTypes.firstNotNullOfOrNull { reference ->
        (reference as? ParameterizedTypeName)
            ?.takeIf { type ->
                type.rawType == ClassNames.TypeConverter
            }
    } ?: throw IllegalStateException("Error typeConverter super for ${className} not found." +
        "${declaration.superTypes.toList()}")
    return typeConverterSuper
}
