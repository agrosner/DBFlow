package com.dbflow5.ksp

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.DatabaseHolderModel
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.ManyToManyModel
import com.dbflow5.ksp.model.MigrationModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.model.OneToManyModel
import com.dbflow5.ksp.model.TypeConverterModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.partOfDatabaseAsType
import com.dbflow5.ksp.model.properties.DatabaseHolderProperties
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseHolderWriter
import com.dbflow5.ksp.writer.DatabaseWriter
import com.dbflow5.ksp.writer.InlineTypeConverterWriter
import com.dbflow5.ksp.writer.ManyToManyClassWriter
import com.dbflow5.ksp.writer.OneToManyClassWriter
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Description:
 */
class DBFlowKspProcessor(
    private val ksClassDeclarationParser: KSClassDeclarationParser,
    private val classWriter: ClassWriter,
    private val databaseWriter: DatabaseWriter,
    private val databaseHolderWriter: DatabaseHolderWriter,
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
    private val environment: SymbolProcessorEnvironment,
    private val typeConverterWriter: InlineTypeConverterWriter,
    private val manyClassWriter: ManyToManyClassWriter,
    private val oneToManyClassWriter: OneToManyClassWriter,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        typeConverterCache.applyResolver(resolver)
        ksClassDeclarationParser.applyResolver(resolver)

        val symbols =
            Annotations.values().map {
                resolver.getSymbolsWithAnnotation(
                    it.qualifiedName
                ).toList()
            }.flatten()
                .distinct()
        if (symbols.isNotEmpty()) {
            val objects = symbols.mapNotNull { annotated ->
                when (annotated) {
                    is KSClassDeclaration -> {
                        ksClassDeclarationParser.parse(annotated)
                    }
                    else -> null
                }
            }.flatten()
            val manyToManyModels = objects.filterIsInstance<ManyToManyModel>()
            val oneToManyModels = objects.filterIsInstance<OneToManyModel>()
            val classes = objects.filterIsInstance<ClassModel>()
                // append all expected classes
                .let {
                    it.toMutableList()
                        .apply {
                            addAll(manyToManyModels.map { model -> model.classModel })
                            addAll(oneToManyModels.map { model -> model.classModel })
                        }
                }
            val migrations = objects.filterIsInstance<MigrationModel>()

            // associate classes into DB.
            val databases = objects.filterIsInstance<DatabaseModel>()
                .map { database ->
                    database.copy(
                        tables = classes.filter {
                            it.partOfDatabaseAsType<ClassModel.ClassType.Normal>(database.classType)
                        },
                        views = classes.filter {
                            it.partOfDatabaseAsType<ClassModel.ClassType.View>(database.classType)
                        },
                        queryModels = classes.filter {
                            it.partOfDatabaseAsType<ClassModel.ClassType.Query>(database.classType)
                        },
                        migrations = migrations.filter {
                            it.properties.database == database.classType
                        },
                    )
                }

            val holderModel = DatabaseHolderModel(
                name = NameModel(ClassNames.GeneratedDatabaseHolder),
                databases,
                properties = DatabaseHolderProperties(""),
                allOriginatingFiles = objects.mapNotNull { it.originatingFile }
            )

            referencesCache.allTables = classes

            objects.filterIsInstance<TypeConverterModel>().forEach { model ->
                typeConverterCache.putTypeConverter(model)
            }

            objects.filterIsInstance<ClassModel>()
                .asSequence()
                .map { it.fields }
                .flatten()
                .mapNotNull { it.properties?.typeConverterClassName }
                .filter { it != ClassNames.TypeConverter }
                .forEach { typeConverterName ->
                    typeConverterCache.putTypeConverter(typeConverterName, resolver)
                }

            typeConverterCache.processNestedConverters()

            listOf(
                typeConverterCache.generatedTypeConverters.map(typeConverterWriter::create),
                classes.map(classWriter::create),
                databases.map(databaseWriter::create),
                listOf(holderModel).map(databaseHolderWriter::create),
                manyToManyModels.map(manyClassWriter::create),
                oneToManyModels.map(oneToManyClassWriter::create),
            )
                .flatten()
                .forEach {
                    it.writeTo(System.out)
                    it.writeTo(
                        environment.codeGenerator,
                        aggregating = false,
                    )
                }
        }

        return listOf()
    }
}