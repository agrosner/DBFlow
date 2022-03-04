package com.dbflow5.codegen.shared.cache

import com.dbflow5.codegen.shared.AssociatedType
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.Platforms
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.properties.TypeConverterProperties
import com.dbflow5.codegen.shared.toChained
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.converter.BigDecimalConverter
import com.dbflow5.converter.BigIntegerConverter
import com.dbflow5.converter.BlobConverter
import com.dbflow5.converter.BooleanConverter
import com.dbflow5.converter.CalendarConverter
import com.dbflow5.converter.CharConverter
import com.dbflow5.converter.DateConverter
import com.dbflow5.converter.SqlDateConverter
import com.dbflow5.converter.UUIDConverter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.javapoet.toJTypeName
import kotlin.reflect.KClass

/**
 * Description: Keeps all defined [TypeConverterModel]
 */
class TypeConverterCache(
    private val platforms: Platforms,
) {

    private val typeConverters = mutableMapOf<AssociatedType, TypeConverterModel>()

    /**
     * Set of TypeConverters to generate
     */
    private val typeConvertersToWrite = mutableSetOf<TypeConverterModel>()
    val generatedTypeConverters: Set<TypeConverterModel> = typeConvertersToWrite

    fun applyResolver(resolver: ClassNameResolver) {
        defaultConverterMap.forEach { (defaultType, platforms) ->
            // if supported platform include it here.
            if (platforms.contains(this.platforms.currentPlatform)) {
                val typeName = defaultType.asClassName()
                putTypeConverter(typeName, resolver)
            }
        }
    }

    /**
     * Discover type converters in the map that are nested,
     * and chain them
     */
    fun processNestedConverters() {
        val reformedCache = typeConverters.map { (classType, converter) ->
            if (typeConverters.any { converter.dataTypeName in it.key }) {
                var activeConverter = converter
                var chainedConverter = when (converter) {
                    is TypeConverterModel.Chained -> converter
                    is TypeConverterModel.Simple -> converter.toChained()
                }
                while (typeConverters.any { activeConverter.dataTypeName in it.key }) {
                    activeConverter =
                        typeConverters.entries.first { activeConverter.dataTypeName in it.key }.value
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
            originatingSource = declaration.containingFile,
        )
        putTypeConverter(classModel)
    }

    fun putTypeConverter(typeConverterModel: TypeConverterModel) {
        val typeName = typeConverterModel.modelTypeName.copy(nullable = false)
        typeConverters[AssociatedType(typeName, typeName.toJTypeName(true))] =
            typeConverterModel
    }

    operator fun get(typeName: TypeName, name: String) =
        typeConverters.entries.firstOrNull { typeName.copy(nullable = false) in it.key }
            ?.value
            ?: throw IllegalStateException("Missing Key ${typeName}:${name}. Map is ${typeConverters}")

    fun has(typeName: TypeName): Boolean = typeConverters.any {
        typeName
            .copy(nullable = false) in it.key
    }

    companion object {

        private val jvmPlatforms = listOf("JVM")
        private val allPlatforms = listOf("JVM", "JS", "NATIVE")

        /**
         * Map of class to supported platforms. If empty, we assume all
         * platforms.
         */
        private val defaultConverterMap = mapOf<KClass<*>, List<String>>(
            CalendarConverter::class to jvmPlatforms,
            BigDecimalConverter::class to jvmPlatforms,
            BigIntegerConverter::class to jvmPlatforms,
            DateConverter::class to jvmPlatforms,
            SqlDateConverter::class to jvmPlatforms,
            BooleanConverter::class to allPlatforms,
            UUIDConverter::class to jvmPlatforms,
            CharConverter::class to allPlatforms,
            BlobConverter::class to allPlatforms,
        )
    }
}

fun extractTypeConverter(
    declaration: ClassDeclaration,
    typeName: TypeName
): ParameterizedTypeName {
    val typeConverterSuper = declaration.superTypes.firstNotNullOfOrNull { reference ->
        (reference as? ParameterizedTypeName)
            ?.takeIf { type ->
                type.rawType == ClassNames.TypeConverter
            }
    } ?: throw ValidationException(
        "Error typeConverter super for $typeName not found. Ensure your class directly " +
            "extends TypeConverter. Subclass hierachies are not allowed!" +
            "${declaration.superTypes.toList()}"
    )
    return typeConverterSuper
}
