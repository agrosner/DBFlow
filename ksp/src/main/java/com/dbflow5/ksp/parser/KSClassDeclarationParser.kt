package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.GranularNotifications
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
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
import com.dbflow5.codegen.shared.companion
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.ModelViewQueryProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.codegen.shared.validation.ValidationExceptionProvider
import com.dbflow5.ksp.model.interop.KSPClassDeclaration
import com.dbflow5.ksp.model.interop.KSPClassType
import com.dbflow5.ksp.model.interop.KSPOriginatingSource
import com.dbflow5.ksp.model.interop.adapterParamsForFunParams
import com.dbflow5.ksp.model.invoke
import com.dbflow5.ksp.model.ksName
import com.dbflow5.ksp.parser.annotation.DatabasePropertyParser
import com.dbflow5.ksp.parser.annotation.Fts4Parser
import com.dbflow5.ksp.parser.annotation.ManyToManyParser
import com.dbflow5.ksp.parser.annotation.MigrationParser
import com.dbflow5.ksp.parser.annotation.MultipleManyToManyParser
import com.dbflow5.ksp.parser.annotation.OneToManyPropertyParser
import com.dbflow5.ksp.parser.annotation.QueryPropertyParser
import com.dbflow5.ksp.parser.annotation.TablePropertyParser
import com.dbflow5.ksp.parser.annotation.TypeConverterPropertyParser
import com.dbflow5.ksp.parser.annotation.ViewPropertyParser
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
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
    private val manyToManyParser: ManyToManyParser,
    private val multipleManyToManyParser: MultipleManyToManyParser,
    private val oneToManyPropertyParser: OneToManyPropertyParser,
    private val fts4Parser: Fts4Parser,
    private val migrationParser: MigrationParser,
) : Parser<KSClassDeclaration, List<ObjectModel>> {

    sealed interface Validation : ValidationExceptionProvider {
        data class QualifiedName(
            val className: ClassName
        ) : Validation {
            override val message: String = "Missing Qualified name for class $className"
        }

        data class CouldNotFindTypeConverter(
            val className: ClassName,
        ) : Validation {
            override val message: String = "Could not find TypeConverter superclass for $className"
        }

        data class MissingTable(
            val annotationName: String,
            val className: ClassName,
        ) : Validation {
            override val message: String =
                "Missing @Table: $className must also declare a Table annotation with $annotationName"
        }

        data class MissingModelViewQuery(
            val className: ClassName,
        ) : Validation {
            override val message: String =
                "Missing @ModelViewQuery: Could not find a ModelViewQuery for $className. It should " +
                    "exist as a companion object field that is accessible."
        }

        data class InvalidConstructor(
            val className: ClassName,
            val fieldsCount: Int,
            val constructorCount: Int? = null,
        ) : Validation {
            override val message: String =
                "Invalid constructor: Could not find a valid constructor to create the model " +
                    "$className. Ensure either all properties are in constructor, or a single, default " +
                    "constructor exists. Found ($fieldsCount) fields " +
                    "${
                        if (constructorCount != null) " for ($constructorCount) " +
                            "constructor fields" else ""
                    }."
        }

        data class InvalidSuperType(
            val className: ClassName,
            val expectedSuperType: ClassName,
        ) : Validation {
            override val message: String =
                "Invalid supertype: Expected $expectedSuperType for $className."
        }
    }

    private lateinit var resolver: Resolver

    /**
     * This needs injecting at execution time.
     */
    fun applyResolver(resolver: Resolver) {
        this.resolver = resolver
    }

    @Throws(ValidationException::class)
    override fun parse(input: KSClassDeclaration): List<ObjectModel> {
        val classType = input.asStarProjectedType().toClassName()
        val qualifiedName = input.qualifiedName ?: throw Validation.QualifiedName(
            input.toClassName()
        ).exception
        val packageName = input.packageName
        val name = NameModel(qualifiedName, packageName)
        val originatingFile = KSPOriginatingSource(input.containingFile)

        // inspect annotations for what object it is.
        return input.annotations.mapNotNull { annotation ->
            return@mapNotNull when (annotation.annotationType.toTypeName()) {
                typeNameOf<Database>() -> {
                    if (!input.hasSuperType(ClassNames.DBFlowDatabase)) {
                        throw Validation.InvalidSuperType(
                            classType,
                            ClassNames.DBFlowDatabase,
                        ).exception
                    }
                    listOf(
                        DatabaseModel(
                            name = name,
                            classType = classType,
                            properties = databasePropertyParser.parse(annotation),
                            originatingSource = originatingFile,
                            adapterFields = input.getAllProperties()
                                .filter { it.isAbstract() }
                                .filter {
                                    val toClassName = it.type.resolve().toTypeName()
                                    toClassName is ParameterizedTypeName &&
                                        (toClassName.rawType in ClassAdapterFieldModel.Type.values()
                                            .map { type -> type.className })
                                }
                                .map {
                                    ClassAdapterFieldModel(
                                        NameModel(it.simpleName, it.packageName),
                                        typeName = it.type.toTypeName() as ParameterizedTypeName,
                                    )
                                }.toList(),
                        )
                    )
                }
                typeNameOf<TypeConverter>() -> {
                    val typeConverterSuper = extractTypeConverter(
                        KSPClassDeclaration(input),
                        classType
                    )
                    listOf(
                        TypeConverterModel.Simple(
                            name = name,
                            properties = typeConverterPropertyParser.parse(annotation),
                            classType = classType,
                            dataTypeName = typeConverterSuper.typeArguments[0],
                            modelTypeName = typeConverterSuper.typeArguments[1],
                            modelClass = null,
                            originatingSource = originatingFile,
                        )
                    )
                }
                typeNameOf<ManyToMany>() -> {
                    listOf(manyToManyModel(input, classType, name, annotation, originatingFile))
                }
                typeNameOf<MultipleManyToMany>() -> {
                    val tableProperties = tablePropertyParser.parse(
                        input.firstOrNull<Table>() ?: throw Validation.MissingTable(
                            "ManyToMany", classType
                        ).exception
                    )
                    multipleManyToManyParser.parse(
                        MultipleManyToManyParser.Input(
                            annotation = annotation,
                            name = name,
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            ksClassDeclaration = input,
                            originatingSource = originatingFile,
                        )
                    )
                }
                typeNameOf<OneToManyRelation>() -> {
                    val tableProperties = tablePropertyParser.parse(
                        input.firstOrNull<Table>()
                            ?: throw Validation.MissingTable(
                                "OneToManyRelation",
                                classType,
                            ).exception
                    )
                    listOf(
                        OneToManyModel(
                            name = name,
                            properties = oneToManyPropertyParser.parse(annotation),
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            ksType = KSPClassType(input.asStarProjectedType()),
                            originatingSource = originatingFile,
                        )
                    )
                }
                typeNameOf<Migration>() -> {
                    val constructor = input.primaryConstructor
                        ?: throw ValidationException("Migration classes must use a primary constructor.")
                    listOf(
                        MigrationModel(
                            name = name,
                            properties = migrationParser.parse(annotation),
                            classType = classType,
                            originatingSource = originatingFile,
                            adapterParams = adapterParamsForFunParams(
                                constructor.parameters
                            ) { typeName -> typeName.rawType == ClassNames.ModelAdapter2 }
                        )
                    )
                }
                else -> handleClass(
                    input,
                    classType,
                    annotation,
                    name,
                    originatingFile
                )
            }
        }
            .flatten()
            .toList()

    }

    private fun handleClass(
        input: KSClassDeclaration,
        classType: ClassName,
        annotation: KSAnnotation,
        name: NameModel,
        originatingSource: KSPOriginatingSource,
    ): List<ObjectModel> {
        val isInternal = input.isInternal()
        val emptyConstructor =
            input.primaryConstructor?.takeIf { declaration -> declaration.parameters.all { it.hasDefault } }
                ?: input.getConstructors().firstNotNullOfOrNull { constructor ->
                    constructor.takeIf { declaration ->
                        declaration.parameters.isEmpty() ||
                            declaration.parameters.all { it.hasDefault }
                    }
                }
        val hasDefaultConstructor = emptyConstructor != null
        val kspClassDeclaration = KSPClassDeclaration(input)
        val isData = kspClassDeclaration.isData
        val fields = fieldSanitizer.parse(input = kspClassDeclaration)
        val implementsLoadFromCursorListener = input
            .superTypes.any { it.toTypeName() == ClassNames.LoadFromCursorListener }
        val implementsSQLiteStatementListener = input
            .superTypes.any { it.toTypeName() == ClassNames.DatabaseStatementListener }
        val granularNotifications = input.firstOrNull<GranularNotifications>() != null
        if (emptyConstructor == null) {
            // find constructor that matches exactly by name.
            input.getConstructors().firstNotNullOf {
                if (fields.size != it.parameters.size) {
                    throw Validation.InvalidConstructor(
                        classType,
                        fieldsCount = fields.size,
                        constructorCount = it.parameters.size,
                    ).exception // not same length, throw error
                } else {
                    fields.all { field ->
                        it.parameters.all { p ->
                            p.name == field.name.ksName &&
                                p.type.toTypeName() == field.classType
                        }
                    }
                }
            }
        }
        val type = KSPClassType(input.asStarProjectedType())
        return when (annotation.annotationType.toTypeName()) {
            typeNameOf<Table>() -> {
                val fts3 = input.annotations.firstOrNull {
                    it.annotationType.toTypeName() == typeNameOf<Fts3>()
                }
                val fts4 = input.annotations.firstOrNull {
                    it.annotationType.toTypeName() == typeNameOf<Fts4>()
                }
                val properties = tablePropertyParser.parse(annotation)
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        ksClassType = type,
                        isDataClass = isData,
                        type = when {
                            fts3 != null -> ClassModel.Type.Table.Fts3
                            fts4 != null -> fts4Parser.parse(fts4)
                            else -> ClassModel.Type.Table.Normal
                        },
                        properties = properties,
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        granularNotifications = granularNotifications,
                        originatingSource = originatingSource,
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        indexGroups = properties.indexGroupProperties
                            .map { group -> group.toModel(classType, fields) },
                        uniqueGroups = properties.uniqueGroupProperties
                            .map { group -> group.toModel(fields) }
                    ))
            }
            typeNameOf<ModelView>() -> {
                // retrieve companion to pull its query field.
                val companion = resolver.getClassDeclarationByName(
                    name = name.companion().ksName
                )
                val modelViewQuery = companion?.let { modelViewQueryOrNull(companion) }
                    ?: modelViewQueryOrNull(input)
                    ?: throw Validation.MissingModelViewQuery(
                        classType,
                    ).exception
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        ksClassType = type,
                        isDataClass = isData,
                        type = ClassModel.Type.View(
                            ModelViewQueryProperties(
                                NameModel(
                                    modelViewQuery.simpleName,
                                    modelViewQuery.packageName
                                ),
                                adapterParams = adapterParamsForFunParams(
                                    modelViewQuery.parameters
                                ) { typeName -> typeName.rawType == ClassNames.ModelAdapter2 }
                            )
                        ),
                        properties = viewPropertyParser.parse(annotation),
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        originatingSource = originatingSource,
                        indexGroups = listOf(),
                        uniqueGroups = listOf(),
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        granularNotifications = false,
                    )
                )
            }
            typeNameOf<Query>() -> {
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        ksClassType = type,
                        isDataClass = isData,
                        type = ClassModel.Type.Query,
                        properties = queryPropertyParser.parse(annotation),
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = isInternal,
                        originatingSource = originatingSource,
                        indexGroups = listOf(),
                        uniqueGroups = listOf(),
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        granularNotifications = false,
                    )
                )
            }
            else -> listOf()
        }
    }

    private fun modelViewQueryOrNull(
        declaration: KSClassDeclaration,
    ): KSFunctionDeclaration? = declaration.getAllFunctions()
        .firstOrNull { it.hasAnnotation<ModelViewQuery>() }

    private fun manyToManyModel(
        input: KSClassDeclaration,
        classType: ClassName,
        name: NameModel,
        annotation: KSAnnotation,
        originatingSource: KSPOriginatingSource
    ): ManyToManyModel {
        val tableProperties = tablePropertyParser.parse(
            input.firstOrNull<Table>() ?: throw Validation.MissingTable(
                "ManyToMany", classType
            ).exception
        )
        return manyToManyParser.parse(
            ManyToManyParser.Input(
                annotation = annotation,
                name = name,
                classType = classType,
                databaseTypeName = tableProperties.database,
                ksClassDeclaration = input,
                originatingSource = originatingSource
            )
        )
    }
}