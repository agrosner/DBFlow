package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.generateTypeConverter
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.parser.FieldSanitizer.Validation.OnlyOneKind
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.model.interop.KSPClassDeclaration
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

class KSPFieldSanitizer(
    private val propertyParser: KSPropertyDeclarationParser,
    private val typeConverterCache: TypeConverterCache,
) : FieldSanitizer {

    @Throws(ValidationException::class)
    override fun parse(input: ClassDeclaration): List<FieldModel> {
        val declaration = (input as KSPClassDeclaration).ksClassDeclaration!!
        val isTable = declaration.hasAnnotation<Table>()
        val isModelView = declaration.hasAnnotation<ModelView>()
        val isQuery = declaration.hasAnnotation<Query>()
        if (listOf(isTable, isModelView, isQuery).count { it } > 1) {
            throw OnlyOneKind(declaration.toClassName()).exception
        }
        // grabs all fields from current class and super class fields.
        return (declaration.getAllProperties() + declaration.superTypes.mapNotNull {
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