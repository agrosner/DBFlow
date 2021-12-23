package com.dbflow5.ksp.parser.extractors

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.OneToMany
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.generateTypeConverter
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.Parser
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description: Extracts valid field types.
 */
class FieldSanitizer(
    private val propertyParser: KSPropertyDeclarationParser,
    private val typeConverterCache: TypeConverterCache,
) : Parser<KSClassDeclaration,
    List<FieldModel>> {

    override fun parse(input: KSClassDeclaration): List<FieldModel> {
        // grabs all fields from current class and super class fields.
        return (input.getAllProperties() + input.superTypes.mapNotNull {
            it.resolve().declaration.closestClassDeclaration()
        }.mapNotNull { it.getAllProperties() }
            .flatten())
            .filterNot { it.isAbstract() }
            .distinctBy { it.simpleName.getShortName() }.filterNot { prop ->
                isIgnoredColumn(prop) || isModelAdapter(prop)
                    || isOneToMany(prop)
                    || prop.isDelegated()
            }
            .map(propertyParser::parse)
            .toList()
            .also { list ->
                // any inline class should put a generated type.
                list.filter { it.isInlineClass }
                    .forEach { inlineType ->
                        typeConverterCache.putGeneratedTypeConverter(
                            inlineType.generateTypeConverter()
                        )
                    }
            }
    }

    private fun isModelAdapter(prop: KSPropertyDeclaration) =
        prop.type.toTypeName() == ClassNames.modelAdapter(ClassNames.BaseModel)

    private fun isIgnoredColumn(prop: KSPropertyDeclaration) =
        checkAllPropAnnotations(prop) {
            it.annotationType.toTypeName() == typeNameOf<ColumnIgnore>()
        }

    private fun isOneToMany(prop: KSPropertyDeclaration): Boolean = checkAllPropAnnotations(prop) {
        it.annotationType.toTypeName() == typeNameOf<OneToMany>()
    }

    private fun checkAllPropAnnotations(
        prop: KSPropertyDeclaration,
        predicate: (KSAnnotation) -> Boolean
    ) =
        prop.annotations.any(predicate)
            || prop.getter?.annotations?.any(predicate) == true
            || prop.setter?.annotations?.any(predicate) == true
}