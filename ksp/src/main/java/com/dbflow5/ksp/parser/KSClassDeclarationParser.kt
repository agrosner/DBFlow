package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.QueryModel
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
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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
    private val oneToManyPropertyParser: OneToManyPropertyParser,
    private val fts4Parser: Fts4Parser,
    private val migrationParser: MigrationParser,
) : Parser<KSClassDeclaration, List<ObjectModel>> {

    private lateinit var resolver: Resolver

    /**
     * This needs injecting at execution time.
     */
    fun applyResolver(resolver: Resolver) {
        this.resolver = resolver
    }

    override fun parse(input: KSClassDeclaration): List<ObjectModel> {
        val classType = input.asStarProjectedType().toClassName()
        val qualifiedName = input.qualifiedName!!
        val packageName = input.packageName
        val hasDefaultConstructor =
            input.primaryConstructor?.parameters?.all { it.hasDefault } == true
                || input.getConstructors().any { constructor ->
                constructor.parameters.isEmpty() ||
                    constructor.parameters.all { it.hasDefault }
            }
        val isInternal = input.isInternal()

        // inspect annotations for what object it is.
        return input.annotations.mapNotNull { annotation ->
            val name = NameModel(qualifiedName, packageName)
            val originatingFile = input.containingFile
            when (annotation.annotationType.toTypeName()) {
                typeNameOf<Database>() -> {
                    DatabaseModel(
                        name = name,
                        classType = classType,
                        properties = databasePropertyParser.parse(annotation),
                        originatingFile = originatingFile,
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
                        name = name,
                        properties = typeConverterPropertyParser.parse(annotation),
                        classType = classType,
                        dataTypeName = typeConverterSuper.typeArguments[0],
                        modelTypeName = typeConverterSuper.typeArguments[1],
                        modelClass = null,
                        originatingFile = originatingFile,
                    )
                }
                typeNameOf<ManyToMany>() -> {
                    val tableProperties = tablePropertyParser.parse(input.first<Table>())
                    ManyToManyModel(
                        name = name,
                        properties = manyToManyPropertyParser.parse(annotation),
                        classType = classType,
                        databaseTypeName = tableProperties.database,
                        ksType = input.asStarProjectedType(),
                        originatingFile = originatingFile,
                    )
                }
                typeNameOf<OneToManyRelation>() -> {
                    val tableProperties = tablePropertyParser.parse(input.first<Table>())
                    OneToManyModel(
                        name = name,
                        properties = oneToManyPropertyParser.parse(annotation),
                        classType = classType,
                        databaseTypeName = tableProperties.database,
                        ksType = input.asStarProjectedType(),
                        originatingFile = originatingFile,
                    )
                }
                typeNameOf<Migration>() -> {
                    MigrationModel(
                        name = name,
                        properties = migrationParser.parse(annotation),
                        classType = classType,
                        originatingFile = originatingFile,
                    )
                }
                else -> {
                    val fields = fieldSanitizer.parse(input = input)
                    when (annotation.annotationType.toTypeName()) {
                        typeNameOf<Table>() -> {
                            val fts3 = input.annotations.firstOrNull {
                                it.annotationType.toTypeName() == typeNameOf<Fts3>()
                            }
                            val fts4 = input.annotations.firstOrNull {
                                it.annotationType.toTypeName() == typeNameOf<Fts4>()
                            }
                            val properties = tablePropertyParser.parse(annotation)
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
                            )
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
                                ?: throw IllegalStateException(
                                    "Could not find ModelViewQuery in definition " +
                                        "${
                                            companion.getAllFunctions().toList()
                                        }: ${companion.getAllProperties().toList()}: " +
                                        "${companion.getDeclaredFunctions().toList()}"
                                )
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
                            )
                        }
                        typeNameOf<QueryModel>() -> {
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
                            )
                        }
                        else -> null
                    }
                }
            }
        }.toList()

    }
}