package com.dbflow5.processor.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Unique
import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.ReferenceHolderModel
import com.dbflow5.codegen.model.SingleFieldModel
import com.dbflow5.codegen.model.properties.NotNullProperties
import com.dbflow5.codegen.model.properties.ReferenceHolderProperties
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.processor.interop.KaptClassType
import com.dbflow5.processor.interop.KaptOriginatingFileType
import com.dbflow5.processor.interop.invoke
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toClassName
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class VariableElementParser(
    private val notNullPropertyParser: NotNullPropertyParser,
    private val indexParser: IndexParser,
    private val fieldPropertyParser: FieldPropertyParser,
    private val uniquePropertyParser: UniquePropertyParser,
    private val foreignKeyParser: ForeignKeyParser,
    private val columnMapParser: ColumnMapParser,
) : Parser<VariableElement, FieldModel> {

    override fun parse(input: VariableElement): FieldModel {
        val primaryKey = input.annotation<PrimaryKey>()
        val fieldType = if (primaryKey != null) {
            FieldModel.FieldType.PrimaryAuto(
                isAutoIncrement = primaryKey.autoincrement,
                isRowId = primaryKey.rowID,
            )
        } else {
            FieldModel.FieldType.Normal
        }
        val kaptClassType = KaptClassType(input.asType(), input)
        val isEnum = input.kind == ElementKind.ENUM
        val foreignKey = input.annotation<ForeignKey>()
        val columnMapKey = input.annotation<ColumnMap>()
        val notNull = input.annotation<NotNull>()?.let {
            notNullPropertyParser.parse(it)
        } ?: if (!kaptClassType.isMarkedNullable) NotNullProperties() else null
        // in KSP this turns java platform into nullable.
        val classType = kaptClassType.makeNotNullable()
        val name = NameModel(
            input.simpleName,
            input.getPackage(),
            // TODO: infer from annotation
            nullable = false,
        )
        // TODO: infer mutability from enclosing class by finding setter
        val isVal = false
        val indexProperties = input.annotation<Index>()
            ?.let { indexParser.parse(it) }
        val properties = input.annotation<Column>()
            ?.let { fieldPropertyParser.parse(it) }
        val uniqueProperties = input.annotation<Unique>()
            ?.let { uniquePropertyParser.parse(it) }
        val enclosingClassType = input.enclosingElement
            .toClassName()!!.toKTypeName()
        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = name,
                classType = classType.toTypeName(),
                fieldType = fieldType,
                properties = properties,
                referenceHolderProperties = foreignKey?.let {
                    foreignKeyParser.parse(it)
                } ?: columnMapKey?.let { columnMapParser.parse(it) }
                ?: ReferenceHolderProperties(
                    referencesType = ReferenceHolderProperties.ReferencesType.All,
                    referencedTableTypeName = Any::class.asTypeName(),
                    deferred = false,
                    saveForeignKeyModel = false,
                ),
                enclosingClassType = enclosingClassType,
                type = if (foreignKey != null) {
                    ReferenceHolderModel.Type.ForeignKey
                } else {
                    ReferenceHolderModel.Type.Computed
                },
                // don't exist in KAPT, do they?
                isInlineClass = false,
                ksClassType = kaptClassType,
                isVal = isVal,
                isColumnMap = columnMapKey != null,
                isEnum = isEnum,
                originatingFile = KaptOriginatingFileType,
                indexProperties = indexProperties,
                notNullProperties = notNull,
                uniqueProperties = uniqueProperties,
            )
        }
        return SingleFieldModel(
            name = name,
            classType = classType.toTypeName(),
            fieldType = fieldType,
            properties = properties,
            enclosingClassType = enclosingClassType,
            isInlineClass = false,
            isVal = isVal,
            isEnum = isEnum,
            ksClassType = kaptClassType,
            originatingFile = KaptOriginatingFileType,
            indexProperties = indexProperties,
            notNullProperties = notNull,
            uniqueProperties = uniqueProperties,
        )
    }
}