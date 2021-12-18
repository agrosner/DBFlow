package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description:
 */
class KSPropertyDeclarationParser constructor(
    private val fieldPropertyParser: FieldPropertyParser,
    private val referenceHolderPropertyParser: ReferenceHolderProperyParser,
) : Parser<KSPropertyDeclaration, FieldModel> {

    override fun parse(input: KSPropertyDeclaration): FieldModel {
        val primaryKey =
            input.annotations.find { it.annotationType.toTypeName() == typeNameOf<PrimaryKey>() }
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
        val column =
            input.annotations.find { it.annotationType.toTypeName() == typeNameOf<Column>() }
        val foreignKey =
            input.annotations.find { it.annotationType.toTypeName() == typeNameOf<ForeignKey>() }
        val columnMapKey =
            input.annotations.find { it.annotationType.toTypeName() == typeNameOf<ColumnMap>() }
        val classType = input.type.toTypeName()
        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = NameModel(
                    input.simpleName,
                    input.packageName,
                    classType.isNullable,
                ),
                classType = classType,
                fieldType,
                properties = column?.let { fieldPropertyParser.parse(column) },
                referenceHolderProperties = referenceHolderPropertyParser.parse(
                    foreignKey ?: columnMapKey!!
                ),
                enclosingClassType = input.parentDeclaration?.closestClassDeclaration()!!
                    .toClassName(),
                type = if (foreignKey != null) {
                    ReferenceHolderModel.Type.ForeignKey
                } else {
                    ReferenceHolderModel.Type.ColumnMap
                }
            )
        }
        return SingleFieldModel(
            name = NameModel(
                input.simpleName,
                input.packageName,
                classType.isNullable,
            ),
            classType = classType,
            fieldType,
            properties = column?.let { fieldPropertyParser.parse(column) },
            enclosingClassType = input.parentDeclaration?.closestClassDeclaration()!!.toClassName(),
        )
    }
}