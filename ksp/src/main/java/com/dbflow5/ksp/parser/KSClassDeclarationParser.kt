package com.dbflow5.ksp.parser

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
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.IndexGroupModel
import com.dbflow5.ksp.model.ManyToManyModel
import com.dbflow5.ksp.model.MigrationModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.ObjectModel
import com.dbflow5.ksp.model.OneToManyModel
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.model.UniqueGroupModel
import com.dbflow5.ksp.model.companion
import com.dbflow5.ksp.model.properties.ModelViewQueryProperties
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
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.dbflow5.ksp.parser.validation.ValidationException
import com.dbflow5.ksp.parser.validation.ValidationExceptionProvider
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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
        val isInternal = input.isInternal()

        // inspect annotations for what object it is.
        return input.annotations.mapNotNull { annotation ->
            val name = NameModel(qualifiedName, packageName)
            val originatingFile = input.containingFile
            when (annotation.annotationType.toTypeName()) {
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
                            originatingFile = originatingFile,
                        )
                    )
                }
                typeNameOf<TypeConverter>() -> {
                    val typeConverterSuper = input.superTypes.firstNotNullOfOrNull { reference ->
                        reference.resolve().toTypeName().let {
                            it as? ParameterizedTypeName
                        }?.takeIf { type ->
                            type.rawType == ClassNames.TypeConverter
                        }
                    } ?: throw Validation.CouldNotFindTypeConverter(classType).exception
                    listOf(
                        TypeConverterModel.Simple(
                            name = name,
                            properties = typeConverterPropertyParser.parse(annotation),
                            classType = classType,
                            dataTypeName = typeConverterSuper.typeArguments[0],
                            modelTypeName = typeConverterSuper.typeArguments[1],
                            modelClass = null,
                            originatingFile = originatingFile,
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
                            originatingFile = originatingFile,
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
                            ksType = input.asStarProjectedType(),
                            originatingFile = originatingFile,
                        )
                    )
                }
                typeNameOf<Migration>() -> {
                    listOf(
                        MigrationModel(
                            name = name,
                            properties = migrationParser.parse(annotation),
                            classType = classType,
                            originatingFile = originatingFile,
                        )
                    )
                }
                else -> {
                    val emptyConstructor =
                        input.primaryConstructor?.takeIf { declaration -> declaration.parameters.all { it.hasDefault } }
                            ?: input.getConstructors().firstNotNullOfOrNull { constructor ->
                                constructor.takeIf {
                                    it.parameters.isEmpty() ||
                                        it.parameters.all { it.hasDefault }
                                }
                            }
                    val hasDefaultConstructor = emptyConstructor != null
                    val fields = fieldSanitizer.parse(input = input)
                    val implementsLoadFromCursorListener = input
                        .superTypes.any { it.toTypeName() == ClassNames.LoadFromCursorListener }
                    val implementsSQLiteStatementListener = input
                        .superTypes.any { it.toTypeName() == ClassNames.SQLiteStatementListener }
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
                    when (annotation.annotationType.toTypeName()) {
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
                                    type = when {
                                        fts3 != null -> ClassModel.ClassType.Normal.Fts3
                                        fts4 != null -> fts4Parser.parse(fts4)
                                        else -> ClassModel.ClassType.Normal.Normal
                                    },
                                    properties = properties,
                                    fields = fields,
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingFile = originatingFile,
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                    indexGroups = properties.indexGroupProperties
                                        .map { group ->
                                            IndexGroupModel(
                                                name = group.name,
                                                unique = group.unique,
                                                tableTypeName = classType,
                                                fields = fields.filter {
                                                    it.indexProperties?.groups?.contains(
                                                        group.number
                                                    ) == true
                                                }
                                            )
                                        },
                                    uniqueGroups = properties.uniqueGroupProperties
                                        .map { group ->
                                            UniqueGroupModel(
                                                number = group.number,
                                                conflictAction = group.conflictAction,
                                                fields = fields.filter {
                                                    it.uniqueProperties?.groups?.contains(
                                                        group.number
                                                    ) == true
                                                }
                                            )
                                        }
                                ))
                        }
                        typeNameOf<ModelView>() -> {
                            // retrieve companion to pull its query field.
                            val companion = resolver.getClassDeclarationByName(
                                name = name.companion().ksName
                            )
                                ?: input // java may have the field.
                            val modelViewQuery = companion.getAllProperties()
                                .firstOrNull { it.hasAnnotation<ModelViewQuery>() }
                                ?: companion.getAllFunctions()
                                    .firstOrNull { it.hasAnnotation<ModelViewQuery>() }
                                ?: companion.getDeclaredFunctions()
                                    .firstOrNull { it.hasAnnotation<ModelViewQuery>() }
                                ?: throw Validation.MissingModelViewQuery(
                                    classType,
                                ).exception
                            listOf(
                                ClassModel(
                                    name = name,
                                    classType = classType,
                                    type = ClassModel.ClassType.View(
                                        ModelViewQueryProperties(
                                            NameModel(
                                                modelViewQuery.simpleName,
                                                modelViewQuery.packageName
                                            ),
                                            isProperty = modelViewQuery is KSPropertyDeclaration
                                        )
                                    ),
                                    properties = viewPropertyParser.parse(annotation),
                                    fields = fields,
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingFile = originatingFile,
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
                                    properties = queryPropertyParser.parse(annotation),
                                    fields = fields,
                                    hasPrimaryConstructor = !hasDefaultConstructor,
                                    isInternal = isInternal,
                                    originatingFile = originatingFile,
                                    indexGroups = listOf(),
                                    uniqueGroups = listOf(),
                                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                                    implementsSQLiteStatementListener = implementsSQLiteStatementListener,
                                )
                            )
                        }
                        else -> listOf()
                    }
                }
            }
        }
            .flatten()
            .toList()

    }

    private fun manyToManyModel(
        input: KSClassDeclaration,
        classType: ClassName,
        name: NameModel,
        annotation: KSAnnotation,
        originatingFile: KSFile?
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
                originatingFile = originatingFile
            )
        )
    }
}