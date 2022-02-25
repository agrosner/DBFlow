package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.GranularNotifications
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.codegen.shared.ClassAdapterFieldModel
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
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.ModelViewQueryProperties
import com.dbflow5.processor.interop.KaptClassDeclaration
import com.dbflow5.processor.interop.KaptOriginatingSource
import com.dbflow5.processor.interop.KaptTypeElementClassType
import com.dbflow5.processor.interop.adapterParamsForExecutableParams
import com.dbflow5.processor.interop.modelViewQueryOrThrow
import com.dbflow5.processor.interop.name
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.erasure
import com.dbflow5.processor.utils.javaToKotlinType
import com.dbflow5.processor.utils.toTypeErasedElement
import com.grosner.kpoet.typeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import com.squareup.kotlinpoet.typeNameOf
import javax.lang.model.element.ExecutableElement
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
        val name = input.name()
        val classType = input.toTypeErasedElement().javaToKotlinType() as ClassName
        return annotations.mapNotNull { annotation ->
            when (val typeName = annotation.annotationType.typeName.toKTypeName()) {
                typeNameOf<Database>() -> {
                    // TODO: check super type
                    listOf(
                        DatabaseModel(
                            name = name,
                            classType = classType,
                            properties = databasePropertyParser.parse(input.annotation()!!),
                            originatingSource = source,
                            adapterFields = classDeclaration.getters
                                .filter { it.isAbstract }
                                .filter {
                                    val toClassName = it.returnTypeName
                                    toClassName is ParameterizedTypeName &&
                                        toClassName.rawType in ClassAdapterFieldModel.Type.values()
                                        .map { type -> type.className }

                                }.map {
                                    ClassAdapterFieldModel(
                                        it.propertyName,
                                        typeName = it.returnTypeName as ParameterizedTypeName,
                                    )
                                }.toList(),
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
                            adapterParams = adapterParamsForExecutableParams(
                                classDeclaration.constructors.first()
                                    .parameters
                            ) { it.rawType == ClassNames.ModelAdapter },
                        )
                    )
                }
                else -> handleClass(
                    input,
                    classDeclaration,
                    typeName,
                    classType,
                    source
                )
            }
        }.flatten()
    }

    private fun handleClass(
        input: TypeElement,
        classDeclaration: KaptClassDeclaration,
        typeName: TypeName,
        classType: ClassName,
        source: KaptOriginatingSource
    ): List<ObjectModel> {
        val name = input.name()
        val fields = fieldSanitizer.parse(classDeclaration)
        val hasDefaultConstructor = classDeclaration.hasDefaultConstructor
        val isInternal = classDeclaration.isInternal
        val isData = classDeclaration.isData
        val implementsLoadFromCursorListener = classDeclaration
            .superTypes.any { it == ClassNames.LoadFromCursorListener }
        val implementsSQLiteStatementListener = classDeclaration
            .superTypes.any { it == ClassNames.DatabaseStatementListener }
        val type = KaptTypeElementClassType(input.asType(), input)
        val granularNotifications = input.annotation<GranularNotifications>() != null
        return when (typeName) {
            typeNameOf<Table>() -> {
                val fts3: Fts3? = input.annotation()
                val fts4: Fts4? = input.annotation()
                val properties = tablePropertyParser.parse(input.annotation())
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        type = when {
                            fts3 != null -> ClassModel.Type.Table.Fts3
                            fts4 != null -> fts4Parser.parse(fts4)
                            else -> ClassModel.Type.Table.Normal
                        },
                        isDataClass = isData,
                        properties = properties,
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        granularNotifications = granularNotifications,
                        originatingSource = source,
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        indexGroups = properties.indexGroupProperties
                            .map { it.toModel(classType, fields) },
                        uniqueGroups = properties.uniqueGroupProperties
                            .map { it.toModel(fields) },
                        ksClassType = type,
                    )
                )
            }
            typeNameOf<ModelView>() -> {
                val modelViewQueryFun =
                    classDeclaration.modelViewQueryOrThrow(
                        name = name,
                        resolver = resolver
                    )
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        isDataClass = isData,
                        type = ClassModel.Type.View(
                            ModelViewQueryProperties(
                                modelViewQueryFun.simpleName,
                                adapterParams = adapterParamsForExecutableParams(
                                    (modelViewQueryFun.element as ExecutableElement)
                                        .parameters
                                ) { it.rawType == ClassNames.ModelAdapter }
                            ),
                        ),
                        properties = viewPropertyParser.parse(input.annotation()),
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        originatingSource = source,
                        indexGroups = listOf(),
                        uniqueGroups = listOf(),
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        ksClassType = type,
                        granularNotifications = false,
                    )
                )
            }
            typeNameOf<Query>() -> {
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        isDataClass = isData,
                        type = ClassModel.Type.Query,
                        properties = queryPropertyParser.parse(input.annotation()),
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        originatingSource = source,
                        indexGroups = listOf(),
                        uniqueGroups = listOf(),
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        ksClassType = type,
                        granularNotifications = false,
                    )
                )
            }
            else -> listOf()
        }
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