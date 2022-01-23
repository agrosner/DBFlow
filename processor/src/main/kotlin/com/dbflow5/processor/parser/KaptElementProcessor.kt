package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.MigrationModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.ObjectModel
import com.dbflow5.codegen.shared.OneToManyModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.extractTypeConverter
import com.dbflow5.codegen.shared.companion
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.ModelViewQueryProperties
import com.dbflow5.processor.interop.KaptClassDeclaration
import com.dbflow5.processor.interop.KaptOriginatingSource
import com.dbflow5.processor.interop.KaptPropertyDeclaration
import com.dbflow5.processor.interop.KaptTypeElementClassType
import com.dbflow5.processor.interop.annotation
import com.dbflow5.processor.interop.invoke
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.erasure
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.javaToKotlinType
import com.dbflow5.processor.utils.toTypeErasedElement
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
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
    private val viewPropertyParser: ViewPropertyParser,
) : Parser<TypeElement,
    List<ObjectModel>> {

    private lateinit var resolver: ClassNameResolver

    fun applyResolver(resolver: ClassNameResolver) {
        this.resolver = resolver
    }

    override fun parse(input: TypeElement): List<ObjectModel> {
        val source = KaptOriginatingSource(input)
        val classDeclaration = KaptClassDeclaration(input)
        val annotations = elements.getAllAnnotationMirrors(input)
        val name = NameModel(
            input.simpleName,
            input.getPackage(),
        )
        val classType = input.toTypeErasedElement().javaToKotlinType() as ClassName
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
                            originatingSource = source,
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
                            originatingSource = source,
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
                            ksType = KaptTypeElementClassType(
                                input.asType().erasure(),
                                input
                            ),
                            originatingSource = source,
                        )
                    )
                }
                typeNameOf<Migration>() -> {
                    listOf(
                        MigrationModel(
                            name = name,
                            properties = migrationParser.parse(input.annotation()),
                            classType = classType,
                            originatingSource = source,
                        )
                    )
                }
                else -> {
                    val fields = fieldSanitizer.parse(KaptClassDeclaration(input))
                    val hasDefaultConstructor = true
                    val isInternal = classDeclaration.isInternal
                    val implementsLoadFromCursorListener = classDeclaration
                        .superTypes.any { it == ClassNames.LoadFromCursorListener }
                    val implementsSQLiteStatementListener = classDeclaration
                        .superTypes.any { it == ClassNames.SQLiteStatementListener }
                    when (typeName) {
                        typeNameOf<Table>() -> {
                            val fts3: Fts3? = input.annotation()
                            val fts4: Fts4? = input.annotation()
                            val properties = tablePropertyParser.parse(input.annotation())
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
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingSource = source,
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                    indexGroups = properties.indexGroupProperties
                                        .map { it.toModel(classType, fields) },
                                    uniqueGroups = properties.uniqueGroupProperties
                                        .map { it.toModel(fields) },
                                )
                            )
                        }
                        typeNameOf<ModelView>() -> {
                            val companion = resolver.classDeclarationByClassName(
                                name.companion().className,
                            ) ?: KaptClassDeclaration(input)

                            // TODO: check methods
                            val modelViewQuery = companion.properties
                                .firstOrNull { (it as KaptPropertyDeclaration).annotation<ModelViewQuery>() != null }
                                ?: throw IllegalStateException("Missing modelview query ${companion.properties.count()}")

                            listOf(
                                ClassModel(
                                    name = name,
                                    classType = classType,
                                    type = ClassModel.ClassType.View(
                                        ModelViewQueryProperties(
                                            modelViewQuery.simpleName,
                                            isProperty = true, // TODO property check
                                        ),
                                    ),
                                    properties = viewPropertyParser.parse(input.annotation()),
                                    fields = fields,
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingSource = source,
                                    indexGroups = listOf(),
                                    uniqueGroups = listOf(),
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                )
                            )
                        }
                        typeNameOf<Query>() -> {
                            listOf(
                                ClassModel(
                                    name = name,
                                    classType = classType,
                                    type = ClassModel.ClassType.Query,
                                    properties = queryPropertyParser.parse(input.annotation()),
                                    fields = fields,
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingSource = source,
                                    indexGroups = listOf(),
                                    uniqueGroups = listOf(),
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
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
        input: TypeElement,
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