package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.QueryModel
import com.dbflow5.annotation.Table
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.ObjectModel
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description:
 */
class KSClassDeclarationParser(
    private val propertyParser: KSPropertyDeclarationParser,
    private val databasePropertyParser: DatabasePropertyParser,
    private val tablePropertyParser: TablePropertyParser,
    private val queryPropertyParser: QueryPropertyParser,
    private val viewPropertyParser: ViewPropertyParser,
) : Parser<KSClassDeclaration, ObjectModel> {

    override fun parse(input: KSClassDeclaration): ObjectModel {
        val fields = input.getAllProperties().map { propertyParser.parse(it) }.toList()
        val classType = input.asStarProjectedType().toTypeName()
        val name = input.qualifiedName!!

        // inspect annotations for what object it is.
        // only allow one of these kinds
        input.annotations.forEach { annotation ->
            val annotationType = annotation.annotationType.toTypeName()
            if (annotationType == typeNameOf<Database>()) {
                return DatabaseModel(
                    name = name,
                    classType = classType,
                    properties = databasePropertyParser.parse(annotation)
                )
            }
            if (annotationType == typeNameOf<Table>()) {
                return ClassModel(
                    name = name,
                    classType = classType,
                    type = ClassModel.ClassType.Normal,
                    properties = tablePropertyParser.parse(annotation),
                    fields = fields,
                )
            }
            if (annotationType == typeNameOf<ModelView>()) {
                return ClassModel(
                    name = name,
                    classType = classType,
                    type = ClassModel.ClassType.View,
                    properties = viewPropertyParser.parse(annotation),
                    fields = fields,
                )
            }
            if (annotationType == typeNameOf<QueryModel>()) {
                return ClassModel(
                    name = name,
                    classType = classType,
                    type = ClassModel.ClassType.Query,
                    properties = queryPropertyParser.parse(annotation),
                    fields = fields,
                )
            }
        }

        throw IllegalStateException("Invalid class type found $name")
    }
}