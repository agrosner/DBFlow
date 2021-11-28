package com.dbflow5.ksp.parser

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.ksp.model.FieldModel
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description:
 */
class KSPropertyDeclarationParser : Parser<KSPropertyDeclaration, FieldModel> {

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

        return FieldModel(
            name = input.qualifiedName!!,
            classType = input.type.toTypeName(),
            fieldType,
        )
    }
}