package com.dbflow5.processor.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Unique
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.processor.interop.KaptOriginatingSource
import com.dbflow5.processor.interop.KaptPropertyDeclaration
import com.dbflow5.processor.interop.KaptVariableElementClassType
import com.dbflow5.processor.interop.annotation
import com.dbflow5.processor.utils.toClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName

/**
 * Description:
 */
class KaptPropertyElementParser(
    private val notNullPropertyParser: NotNullPropertyParser,
    private val indexParser: IndexParser,
    private val fieldPropertyParser: FieldPropertyParser,
    private val uniquePropertyParser: UniquePropertyParser,
    private val foreignKeyParser: ForeignKeyParser,
    private val columnMapParser: ColumnMapParser,
) : Parser<KaptPropertyDeclaration, FieldModel> {

    override fun parse(input: KaptPropertyDeclaration): FieldModel {
        val source = KaptOriginatingSource(input.element)
        val primaryKey = input.annotation<PrimaryKey>()
        val fieldType = if (primaryKey != null) {
            FieldModel.FieldType.Primary(
                isAutoIncrement = primaryKey.autoincrement,
                isRowId = primaryKey.rowID,
            )
        } else {
            FieldModel.FieldType.Normal
        }
        val kaptClassType = KaptVariableElementClassType(input)
        val isInlineClass = kaptClassType
            .declaration.isValue
        val isEnum = kaptClassType.declaration.closestClassDeclaration?.isEnum
            ?: false
        val foreignKey = input.annotation<ForeignKey>()
        val columnMapKey = input.annotation<ColumnMap>()
        val notNull = input.annotation<NotNull>()?.let {
            notNullPropertyParser.parse(it)
        } ?: if (!kaptClassType.isMarkedNullable) NotNullProperties() else null
        // in KSP this turns java platform into nullable.
        val classType = kaptClassType.toTypeName()
        val name = input.simpleName
        // TODO: infer mutability from enclosing class by finding setter
        val isVal = !kaptClassType.isMutable
        val indexProperties = input.annotation<Index>()
            ?.let { indexParser.parse(it) }
        val properties = input.annotation<Column>()
            ?.let { fieldPropertyParser.parse(it) }
        val uniqueProperties = input.annotation<Unique>()
            ?.let { uniquePropertyParser.parse(it) }
        val enclosingClassType = input.element.enclosingElement
            .toClassName()!!.toKTypeName()
        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = name,
                classType = classType,
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
                isInlineClass = isInlineClass,
                ksClassType = kaptClassType,
                isVal = isVal,
                isColumnMap = columnMapKey != null,
                isEnum = isEnum,
                originatingSource = source,
                indexProperties = indexProperties,
                notNullProperties = notNull,
                uniqueProperties = uniqueProperties,
            )
        }
        return SingleFieldModel(
            name = name,
            classType = classType,
            fieldType = fieldType,
            properties = properties,
            enclosingClassType = enclosingClassType,
            isInlineClass = isInlineClass,
            isVal = isVal,
            isEnum = isEnum,
            ksClassType = kaptClassType,
            originatingSource = source,
            indexProperties = indexProperties,
            notNullProperties = notNull,
            uniqueProperties = uniqueProperties,
        )
    }
}