package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.ksp.kotlinpoet.javaPlatformTypeName
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Description:
 */
class KSPropertyDeclarationParser constructor(
    private val fieldPropertyParser: FieldPropertyParser,
    private val referenceHolderPropertyParser: ReferenceHolderProperyParser,
    private val indexParser: IndexParser,
    private val notNullPropertyParser: NotNullPropertyParser,
) : Parser<KSPropertyDeclaration, FieldModel> {

    override fun parse(input: KSPropertyDeclaration): FieldModel {
        val originatingFile = input.containingFile
        val primaryKey = input.findSingle<PrimaryKey>()
        val fieldType = if (primaryKey != null) {
            val props = primaryKey.arguments.mapProperties()
            FieldModel.FieldType.PrimaryAuto(
                isAutoIncrement = props.arg("autoincrement"),
                isRowId = props.arg("rowID"),
                quickCheckPrimaryKey = props.arg("quickCheckAutoIncrement")
            )
        } else {
            FieldModel.FieldType.Normal
        }
        val ksClassType = input.type.resolve()
        val isInlineClass = ksClassType
            .declaration.modifiers.any { it == Modifier.VALUE }
        val isEnum =
            ksClassType.declaration.closestClassDeclaration()?.classKind == ClassKind.ENUM_CLASS
        val foreignKey = input.findSingle<ForeignKey>()
        val columnMapKey = input.findSingle<ColumnMap>()
        val notNull = input.findSingle<NotNullPropertyParser>()?.let {
            notNullPropertyParser.parse(it)
            // if a field is non-null, then we treat it at DB level
            // TODO: this should be opt-in most likely as it breaks with KAPT / legacy behavior.
        } ?: if (!ksClassType.isMarkedNullable) NotNullProperties() else null
        val classType = input.type.javaPlatformTypeName()
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

        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = name,
                classType = classType,
                fieldType,
                properties = properties,
                referenceHolderProperties = (foreignKey ?: columnMapKey)?.let {
                    referenceHolderPropertyParser.parse(
                        it
                    )
                } ?: ReferenceHolderProperties(
                    referencesType = ReferenceHolderProperties.ReferencesType.All,
                    referencedTableTypeName = Any::class.asTypeName(),
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
                originatingFile = originatingFile,
                indexProperties = indexProperties,
                notNullProperties = notNull,
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
            originatingFile = originatingFile,
            indexProperties = indexProperties,
            notNullProperties = notNull,
        )
    }
}