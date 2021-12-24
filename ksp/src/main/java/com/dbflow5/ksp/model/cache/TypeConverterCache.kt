package com.dbflow5.ksp.model.cache

import com.dbflow5.converter.*
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.*

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache(
    private val logger: KSPLogger,
) {

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
     * Add a type converter we generate on the fly - currently only used for Inline class types.
     */
    fun putGeneratedTypeConverter(typeConverterModel: TypeConverterModel) {
        putTypeConverter(typeConverterModel)
        typeConvertersToWrite.add(typeConverterModel)
    }

    fun putTypeConverter(className: ClassName, resolver: Resolver) {
        val declaration =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(className.toString()))!!
        val typeConverterSuper = extractTypeParameterType(declaration, className)
        val classModel = TypeConverterModel(
            name = NameModel(className),
            properties = TypeConverterProperties(listOf()),
            classType = className,
            dataTypeName = typeConverterSuper.typeArguments[0],
            modelTypeName = typeConverterSuper.typeArguments[1],
            modelClass = declaration.asStarProjectedType().declaration.closestClassDeclaration(),
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
