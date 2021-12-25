package com.dbflow5.ksp.parser

import com.dbflow5.annotation.*
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.*
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.google.devtools.ksp.getConstructors
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
    private val fieldSanitizer: FieldSanitizer,
    private val databasePropertyParser: DatabasePropertyParser,
    private val tablePropertyParser: TablePropertyParser,
    private val queryPropertyParser: QueryPropertyParser,
    private val viewPropertyParser: ViewPropertyParser,
    private val typeConverterPropertyParser: TypeConverterPropertyParser,
    private val manyToManyPropertyParser: ManyToManyPropertyParser,
) : Parser<KSClassDeclaration, List<ObjectModel>> {

    override fun parse(input: KSClassDeclaration): List<ObjectModel> {
        val fields = fieldSanitizer.parse(input = input)
        val classType = input.asStarProjectedType().toClassName()
        val name = input.qualifiedName!!
        val packageName = input.packageName
        val hasDefaultConstructor =
            input.primaryConstructor?.parameters?.all { it.hasDefault } == true
                || input.getConstructors().any { constructor ->
                constructor.parameters.isEmpty() ||
                    constructor.parameters.all { it.hasDefault }
            }
        val isInternal = input.isInternal()

        // inspect annotations for what object it is.
        return input.annotations.map { annotation ->
            when (annotation.annotationType.toTypeName()) {
                typeNameOf<Database>() -> {
                    DatabaseModel(
                        name = NameModel(name, packageName),
                        classType = classType,
                        properties = databasePropertyParser.parse(annotation)
                    )
                }
                typeNameOf<Table>() -> {
                    ClassModel(
                        name = NameModel(name, packageName),
                        classType = classType,
                        type = ClassModel.ClassType.Normal,
                        properties = tablePropertyParser.parse(annotation),
                        fields = fields,
                        hasPrimaryConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                    )
                }
                typeNameOf<ModelView>() -> {
                    ClassModel(
                        name = NameModel(name, packageName),
                        classType = classType,
                        type = ClassModel.ClassType.View,
                        properties = viewPropertyParser.parse(annotation),
                        fields = fields,
                        hasPrimaryConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                    )
                }
                typeNameOf<QueryModel>() -> {
                    ClassModel(
                        name = NameModel(name, packageName),
                        classType = classType,
                        type = ClassModel.ClassType.Query,
                        properties = queryPropertyParser.parse(annotation),
                        fields = fields,
                        hasPrimaryConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                    )
                }
                typeNameOf<TypeConverter>() -> {
                    val typeConverterSuper = input.superTypes.firstNotNullOf { reference ->
                        reference.resolve().toTypeName().let {
                            it as? ParameterizedTypeName
                        }?.takeIf { type ->
                            type.rawType == ClassNames.TypeConverter
                        }
                    }
                    TypeConverterModel.Simple(
                        name = NameModel(name, packageName),
                        properties = typeConverterPropertyParser.parse(annotation),
                        classType = classType,
                        dataTypeName = typeConverterSuper.typeArguments[0],
                        modelTypeName = typeConverterSuper.typeArguments[1],
                        modelClass = null,
                    )
                }
                typeNameOf<ManyToMany>() -> {
                    val tableAnnotation = input.annotations.first {
                        it.annotationType.toTypeName() == typeNameOf<Table>()
                    }
                    val props = tablePropertyParser.parse(tableAnnotation)
                    ManyToManyModel(
                        name = NameModel(name, packageName),
                        properties = manyToManyPropertyParser.parse(annotation),
                        classType = classType,
                        databaseTypeName = props.database,
                        ksType = input.asStarProjectedType(),
                    )
                }
                else -> {
                    throw IllegalStateException("Invalid class type found ${name.asString()}")
                }
            }
        }.toList()

    }
}