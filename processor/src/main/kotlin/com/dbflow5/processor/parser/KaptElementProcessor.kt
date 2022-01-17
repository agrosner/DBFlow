package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.DatabaseModel
import com.dbflow5.codegen.model.ManyToManyModel
import com.dbflow5.codegen.model.MigrationModel
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.ObjectModel
import com.dbflow5.codegen.model.OneToManyModel
import com.dbflow5.codegen.model.TypeConverterModel
import com.dbflow5.codegen.model.cache.extractTypeConverter
import com.dbflow5.codegen.parser.FieldSanitizer
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.ksp.ClassNames
import com.dbflow5.processor.interop.KaptClassDeclaration
import com.dbflow5.processor.interop.KaptClassType
import com.dbflow5.processor.interop.KaptOriginatingFileType
import com.dbflow5.processor.interop.invoke
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.asStarProjectedType
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toTypeErasedElement
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Description:
 */
class KaptElementProcessor(
    private val elements: Elements,
    private val databasePropertyParser: DatabasePropertyParser,
    private val typeConverterPropertyParser: TypeConverterPropertyParser,
    private val oneToManyPropertyParser: OneToManyPropertyParser,
    private val tablePropertyParser: TablePropertyParser,
    private val migrationParser: MigrationParser,
    private val fts4Parser: Fts4Parser,
    private val fieldSanitizer: FieldSanitizer,
    private val manyToManyParser: ManyToManyParser,
    private val queryPropertyParser: QueryPropertyParser,
    private val multipleManyToManyParser: MultipleManyToManyParser,
) : Parser<TypeElement,
    List<ObjectModel>> {

    override fun parse(input: TypeElement): List<ObjectModel> {
        val classDeclaration = KaptClassDeclaration(input)
        val annotations = elements.getAllAnnotationMirrors(input)
        val name = NameModel(
            input.simpleName,
            input.getPackage(),
        )
        val classType = input.toTypeErasedElement()!!.asClassName()
        return annotations.mapNotNull { annotation ->
            val typeName = annotation.annotationType.asTypeName()
            when (typeName) {
                typeNameOf<Database>() -> {
                    // TODO: check super type
                    listOf(
                        DatabaseModel(
                            name = name,
                            classType = classType,
                            properties = databasePropertyParser.parse(input.annotation()!!),
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                typeNameOf<TypeConverter>() -> {
                    val typeConverterSuper = extractTypeConverter(
                        KaptClassDeclaration(input),
                        classType
                    )
                    listOf(
                        TypeConverterModel.Simple(
                            name = name,
                            properties = typeConverterPropertyParser.parse(input.annotation()),
                            classType = classType,
                            dataTypeName = typeConverterSuper.typeArguments[0],
                            modelTypeName = typeConverterSuper.typeArguments[1],
                            modelClass = null,
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                typeNameOf<ManyToMany>() -> {
                    listOf(
                        manyToManyModel(input, classType, name, input.annotation())
                    )
                }
                typeNameOf<MultipleManyToMany>() -> {
                    // TODO: validation
                    val tableProperties = tablePropertyParser.parse(
                        input.annotation()
                    )
                    multipleManyToManyParser.parse(
                        MultipleManyToManyParser.Input(
                            annotation = input.annotation(),
                            name = name,
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            element = input,
                        )
                    )
                }
                typeNameOf<OneToManyRelation>() -> {
                    val tableProperties = tablePropertyParser.parse(
                        input.annotation()
                    )
                    listOf(
                        OneToManyModel(
                            name = name,
                            properties = oneToManyPropertyParser.parse(input.annotation()),
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            ksType = KaptClassType(
                                input.asType().asStarProjectedType(),
                                input
                            ),
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                typeNameOf<Migration>() -> {
                    listOf(
                        MigrationModel(
                            name = name,
                            properties = migrationParser.parse(input.annotation()),
                            classType = classType,
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                else -> {
                    val fields = fieldSanitizer.parse(KaptClassDeclaration(input))
                    when (typeName) {
                        typeNameOf<Table>() -> {
                            val fts3: Fts3? = input.annotation()
                            val fts4: Fts4? = input.annotation()
                            val properties = tablePropertyParser.parse(input.annotation())
                            val implementsLoadFromCursorListener = classDeclaration
                                .superTypes.any { it == ClassNames.LoadFromCursorListener }
                            val implementsSQLiteStatementListener = classDeclaration
                                .superTypes.any { it == ClassNames.SQLiteStatementListener }
                            listOf(
                                ClassModel(
                                    name = name,
                                    classType = classType,
                                    type = when {
                                        fts3 != null -> ClassModel.ClassType.Normal.Fts3
                                        fts4 != null -> fts4Parser.parse(fts4)
                                        else -> ClassModel.ClassType.Normal.Normal
                                    },
                                    properties = properties,
                                    fields = fields,
                                    // TODO: calculate
                                    hasPrimaryConstructor = false,
                                    isInternal = false,
                                    originatingFile = KaptOriginatingFileType,
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                    indexGroups = properties.indexGroupProperties
                                        .map { it.toModel(classType, fields) },
                                    uniqueGroups = properties.uniqueGroupProperties
                                        .map { it.toModel(fields) },
                                )
                            )
                        }
                        else -> listOf()
                    }
                }
            }
        }.flatten()
    }


    private fun manyToManyModel(
        input: Element,
        classType: ClassName,
        name: NameModel,
        annotation: ManyToMany,
    ): ManyToManyModel {
        // TODO: validation
        val tableProperties = tablePropertyParser.parse(
            input.annotation()
        )
        return manyToManyParser.parse(
            ManyToManyParser.Input(
                manyToMany = annotation,
                name = name,
                classType = classType,
                databaseTypeName = tableProperties.database,
                element = input,
            )
        )
    }
}