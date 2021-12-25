package com.dbflow5.ksp.model.cache

import com.dbflow5.converter.BigDecimalConverter
import com.dbflow5.converter.BigIntegerConverter
import com.dbflow5.converter.BlobConverter
import com.dbflow5.converter.BooleanConverter
import com.dbflow5.converter.CalendarConverter
import com.dbflow5.converter.CharConverter
import com.dbflow5.converter.DateConverter
import com.dbflow5.converter.SqlDateConverter
import com.dbflow5.converter.TypeConverter
import com.dbflow5.converter.UUIDConverter
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.dbflow5.ksp.model.toChained
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.*

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

    fun applyResolver(resolver: Resolver) {
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
        resolver: Resolver
    ) {
        val declaration =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(className.toString()))!!
        val typeConverterSuper = extractTypeParameterType(declaration, className)
        val classModel = TypeConverterModel.Simple(
            name = NameModel(className),
            properties = TypeConverterProperties(listOf()),
            classType = className,
            dataTypeName = typeConverterSuper.typeArguments[0],
            modelTypeName = typeConverterSuper.typeArguments[1],
            modelClass = declaration.asStarProjectedType().declaration.closestClassDeclaration(),
            originatingFile = declaration.containingFile,
        )
        putTypeConverter(classModel)
    }

    private fun extractTypeParameterType(
        declaration: KSClassDeclaration,
        className: ClassName
    ): ParameterizedTypeName {
        val typeConverterSuper = declaration.superTypes.firstNotNullOfOrNull { reference ->
            reference.resolve().toTypeName().let {
                it as? ParameterizedTypeName
            }?.takeIf { type ->
                type.rawType == ClassNames.TypeConverter
            }
        } ?: throw IllegalStateException("Error ${className}")
        return typeConverterSuper
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

/**
 * If a [TypeConverter] resolves to another, existing [TypeConverter] type,
 * chain until resolved.
 */
fun TypeConverterCache.chainedReference(
    codeBlock: CodeBlock.Builder,
    typeName: TypeName,
    name: String
) {
    var typeConverter = this[typeName, name]
    codeBlock.add(
        "(%L", typeConverter
            .name.shortName.lowercase(Locale.getDefault())
    )
    while (has(typeConverter.dataTypeName)) {
        codeBlock.add(
            ".%M(%L)", MemberNames.chain, typeConverter
                .name.shortName.lowercase(Locale.getDefault())
        )
        typeConverter = this[typeConverter.dataTypeName, name]
    }
    codeBlock.add(")")
}
