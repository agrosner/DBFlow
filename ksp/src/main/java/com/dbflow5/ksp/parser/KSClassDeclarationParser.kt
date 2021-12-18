package com.dbflow5.ksp.parser

import com.dbflow5.annotation.*
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.*
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toClassName
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
    private val typeConverterPropertyParser: TypeConverterPropertyParser,
) : Parser<KSClassDeclaration, ObjectModel> {

    override fun parse(input: KSClassDeclaration): ObjectModel {
        val fields = input.getAllProperties()
            .filterNot { prop ->
                prop.annotations.any {
                    it.annotationType.toTypeName() == typeNameOf<ColumnIgnore>()
                } || prop.type.toTypeName() == ClassNames.modelAdapter(ClassNames.BaseModel)
            }
            .map { propertyParser.parse(it) }
            .toList()

        val classType = input.asStarProjectedType().toClassName()
        val name = input.qualifiedName!!
        val packageName = input.packageName
        val hasPrimaryConstructor = input.primaryConstructor != null
        val isInternal = input.isInternal()

        // inspect annotations for what object it is.
        // only allow one of these kinds
        input.annotations.forEach { annotation ->
            val annotationType = annotation.annotationType.toTypeName()
            if (annotationType == typeNameOf<Database>()) {
                return DatabaseModel(
                    name = NameModel(name, packageName),
                    classType = classType,
                    properties = databasePropertyParser.parse(annotation)
                )
            }
            if (annotationType == typeNameOf<Table>()) {
                return ClassModel(
                    name = NameModel(name, packageName),
                    classType = classType,
                    type = ClassModel.ClassType.Normal,
                    properties = tablePropertyParser.parse(annotation),
                    fields = fields,
                    hasPrimaryConstructor = hasPrimaryConstructor,
                    isInternal = isInternal,
                )
            }
            if (annotationType == typeNameOf<ModelView>()) {
                return ClassModel(
                    name = NameModel(name, packageName),
                    classType = classType,
                    type = ClassModel.ClassType.View,
                    properties = viewPropertyParser.parse(annotation),
                    fields = fields,
                    hasPrimaryConstructor = hasPrimaryConstructor,
                    isInternal = isInternal,
                )
            }
            if (annotationType == typeNameOf<QueryModel>()) {
                return ClassModel(
                    name = NameModel(name, packageName),
                    classType = classType,
                    type = ClassModel.ClassType.Query,
                    properties = queryPropertyParser.parse(annotation),
                    fields = fields,
                    hasPrimaryConstructor = hasPrimaryConstructor,
                    isInternal = isInternal,
                )
            }
            if (annotationType == typeNameOf<TypeConverter>()) {
                val typeConverterSuper = input.superTypes.firstNotNullOf { reference ->
                    reference.resolve().toTypeName().let {
                        it as? ParameterizedTypeName
                    }?.takeIf { type ->
                        type.rawType == ClassNames.TypeConverter
                    }
                }
                return TypeConverterModel(
                    name = NameModel(name, packageName),
                    properties = typeConverterPropertyParser.parse(annotation),
                    classType = classType,
                    dataClassType = typeConverterSuper.typeArguments[0],
                    modelClassType = typeConverterSuper.typeArguments[1],
                )
            }
        }

        throw IllegalStateException("Invalid class type found ${name.asString()}")
    }
}