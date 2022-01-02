package com.dbflow5.ksp.parser.extractors

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.generateTypeConverter
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.Parser
import com.dbflow5.ksp.parser.hasAnnotation
import com.dbflow5.ksp.parser.validation.ValidationException
import com.dbflow5.ksp.parser.validation.ValidationExceptionProvider
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
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

    sealed interface Validation : ValidationExceptionProvider {

        data class OnlyOneKind(
            val className: ClassName,
        ) : Validation {
            override val message: String = "$className can only contain one of " +
                "the following types: Table, ModelView, or QueryModel"
        }
    }

    @Throws(ValidationException::class)
    override fun parse(input: KSClassDeclaration): List<FieldModel> {
        val isTable = input.hasAnnotation<Table>()
        val isModelView = input.hasAnnotation<ModelView>()
        val isQuery = input.hasAnnotation<Query>()
        if (listOf(isTable, isModelView, isQuery).count { it } > 1) {
            throw Validation.OnlyOneKind(input.toClassName()).exception
        }
        // grabs all fields from current class and super class fields.
        return (input.getAllProperties() + input.superTypes.mapNotNull {
            it.resolve().declaration.closestClassDeclaration()
        }.mapNotNull { it.getAllProperties() }
            .flatten())
            .filterNot { it.isAbstract() }
            .distinctBy { it.simpleName.getShortName() }.filterNot { prop ->
                isIgnoredColumn(prop)
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