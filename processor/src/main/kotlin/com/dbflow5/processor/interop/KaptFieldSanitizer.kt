package com.dbflow5.processor.interop

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.processor.parser.VariableElementParser
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.simpleString
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class KaptFieldSanitizer(
    private val typeConverterCache: TypeConverterCache,
    private val variableElementParser: VariableElementParser,
) : FieldSanitizer {

    override fun parse(input: ClassDeclaration): List<FieldModel> {
        input as KaptClassDeclaration
        val typeElement = input.typeElement!!
        val isTable = typeElement.annotation<Table>() != null
        val isModelView = typeElement.annotation<ModelView>() != null
        val isQuery = typeElement.annotation<Query>() != null
        if (listOf(isTable, isModelView, isQuery).count { it } > 1) {
            throw FieldSanitizer.Validation.OnlyOneKind(typeElement.asClassName()).exception
        }
        return ((input.propertyElements ?: listOf()) + input.superElements.mapNotNull {
            it.toTypeElement()
        }
            .map { it.enclosedElements.filterIsInstance<VariableElement>() }
            .flatten())
            .asSequence()
            .filterNot { it.modifiers.contains(Modifier.ABSTRACT) }
            .distinctBy { it.simpleString }
            .filterNot {
                it.annotation<ColumnIgnore>() != null
                    || it.annotation<OneToMany>() != null
            }
            .map(variableElementParser::parse)
            .toList()
    }
}