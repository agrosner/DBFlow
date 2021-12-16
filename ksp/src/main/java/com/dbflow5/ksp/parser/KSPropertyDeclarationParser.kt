package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ForeignKeyModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description:
 */
class KSPropertyDeclarationParser constructor(
    private val fieldPropertyParser: FieldPropertyParser,
    private val foreignKeyPropertyParser: ForeignKeyPropertyParser,
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
        if (foreignKey != null) {
            return ForeignKeyModel(
                name = NameModel(
                    input.simpleName,
                    input.packageName
                ),
                classType = input.type.toTypeName(),
                fieldType,
                properties = column?.let { fieldPropertyParser.parse(column) },
                foreignKeyProperties = foreignKeyPropertyParser.parse(foreignKey),
            )
        }
        return SingleFieldModel(
            name = NameModel(
                input.simpleName,
                input.packageName
            ),
            classType = input.type.toTypeName(),
            fieldType,
            properties = column?.let { fieldPropertyParser.parse(column) }
        )
    }
}