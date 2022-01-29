package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Unique
import com.dbflow5.ksp.nullablePlatformType
import com.dbflow5.ksp.model.interop.KSPClassType
import com.dbflow5.ksp.model.interop.KSPOriginatingSource
import com.dbflow5.ksp.model.invoke
import com.dbflow5.ksp.parser.annotation.FieldPropertyParser
import com.dbflow5.ksp.parser.annotation.IndexParser
import com.dbflow5.ksp.parser.annotation.NotNullPropertyParser
import com.dbflow5.ksp.parser.annotation.ReferenceHolderPropertyParser
import com.dbflow5.ksp.parser.annotation.UniquePropertyParser
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Description:
 */
class KSPropertyDeclarationParser constructor(
    private val fieldPropertyParser: FieldPropertyParser,
    private val referenceHolderPropertyParser: ReferenceHolderPropertyParser,
    private val indexParser: IndexParser,
    private val notNullPropertyParser: NotNullPropertyParser,
    private val uniquePropertyParser: UniquePropertyParser,
) : Parser<KSPropertyDeclaration, FieldModel> {

    override fun parse(input: KSPropertyDeclaration): FieldModel {
        val originatingFile = KSPOriginatingSource(input.containingFile)
        val primaryKey = input.findSingle<PrimaryKey>()
        val fieldType = if (primaryKey != null) {
            val props = primaryKey.arguments.mapProperties()
            FieldModel.FieldType.Primary(
                isAutoIncrement = props.arg("autoincrement"),
                isRowId = props.arg("rowID"),
            )
        } else {
            FieldModel.FieldType.Normal
        }
        val ksClassType = KSPClassType(input.type.resolve())
        val isInlineClass = ksClassType
            .declaration.isValue
        val isEnum =
            ksClassType.declaration.closestClassDeclaration?.isEnum ?: false
        val foreignKey = input.findSingle<ForeignKey>()
        val columnMapKey = input.findSingle<ColumnMap>()
        val notNull = input.findSingle<NotNull>()?.let {
            notNullPropertyParser.parse(it)
            // if a field is non-null, then we treat it at DB level
            // TODO: this should be opt-in most likely as it breaks with KAPT / legacy behavior.
        } ?: if (!ksClassType.isMarkedNullable) NotNullProperties() else null
        val classType = input.nullablePlatformType()
        val name = NameModel(
            input.simpleName,
            input.packageName,
            classType.isNullable,
        )
        val isVal = !input.isMutable
        val indexProperties = input.findSingle<Index>()
            ?.let { indexParser.parse(it) }
        val properties = input.findSingle<Column>()
            ?.let { fieldPropertyParser.parse(it) }
        val uniqueProperties = input.findSingle<Unique>()
            ?.let { uniquePropertyParser.parse(it) }

        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = name,
                classType = classType,
                fieldType = fieldType,
                properties = properties,
                referenceHolderProperties = (foreignKey ?: columnMapKey)?.let {
                    referenceHolderPropertyParser.parse(
                        it
                    )
                } ?: ReferenceHolderProperties(
                    referencesType = ReferenceHolderProperties.ReferencesType.All,
                    referencedTableTypeName = Any::class.asTypeName(),
                    deferred = false,
                    saveForeignKeyModel = false,
                ),
                enclosingClassType = input.parentDeclaration?.closestClassDeclaration()!!
                    .toClassName(),
                type = if (foreignKey != null) {
                    ReferenceHolderModel.Type.ForeignKey
                } else {
                    ReferenceHolderModel.Type.Computed
                },
                isInlineClass = isInlineClass,
                ksClassType = ksClassType,
                isVal = isVal,
                isColumnMap = columnMapKey != null,
                isEnum = isEnum,
                originatingSource = originatingFile,
                indexProperties = indexProperties,
                notNullProperties = notNull,
                uniqueProperties = uniqueProperties,
            )
        }
        return SingleFieldModel(
            name = name,
            classType = classType,
            fieldType,
            properties = properties,
            enclosingClassType = input.parentDeclaration?.closestClassDeclaration()!!.toClassName(),
            isInlineClass = isInlineClass,
            isVal = isVal,
            isEnum = isEnum,
            ksClassType = ksClassType,
            originatingSource = originatingFile,
            indexProperties = indexProperties,
            notNullProperties = notNull,
            uniqueProperties = uniqueProperties,
        )
    }
}