package com.dbflow5.processor.interop

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
import com.dbflow5.processor.parser.KaptPropertyElementParser
import com.dbflow5.processor.utils.annotation
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class KaptFieldSanitizer(
    private val typeConverterCache: TypeConverterCache,
    private val kaptPropertyElementParser: KaptPropertyElementParser,
) : FieldSanitizer() {

    override fun parse(input: ClassDeclaration): List<FieldModel> {
        input as KaptClassDeclaration
        val typeElement = input.typeElement
        val isTable = typeElement.annotation<Table>() != null
        val isModelView = typeElement.annotation<ModelView>() != null
        val isQuery = typeElement.annotation<Query>() != null
        if (listOf(isTable, isModelView, isQuery).count { it } > 1) {
            throw Validation.OnlyOneKind(typeElement.asClassName()).exception
        }
        return input.properties
            .distinctBy { it.simpleName.shortName }
            .map { it as KaptPropertyDeclaration }
            .filterNot {
                it.isAbstract ||
                    it.annotation<ColumnIgnore>() != null
                    || it.annotation<OneToMany>() != null
                    || it.element.modifiers.contains(Modifier.STATIC)
            }
            .map(kaptPropertyElementParser::parse)
            .toList()
            .also { list ->
                list.filter { it.isInlineClass }
                    .forEach { inlineType ->
                        typeConverterCache.putGeneratedTypeConverter(
                            inlineType.generateTypeConverter(resolver)
                        )
                    }
            }
    }
}