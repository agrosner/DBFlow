package com.dbflow5.ksp

import com.dbflow5.ksp.model.*
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.properties.DatabaseHolderProperties
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseHolderWriter
import com.dbflow5.ksp.writer.DatabaseWriter
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
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            Annotations.values().map {
                resolver.getSymbolsWithAnnotation(
                    it.qualifiedName
                ).toList()
            }.flatten()
        if (symbols.isNotEmpty()) {
            val objects = symbols.mapNotNull { annotated ->
                when (annotated) {
                    is KSClassDeclaration -> {
                        ksClassDeclarationParser.parse(annotated)
                    }
                    else -> null
                }
            }
            val classes = objects.filterIsInstance<ClassModel>()

            // associate classes into DB.
            val databases = objects.filterIsInstance<DatabaseModel>()
                .map { database ->
                    database.copy(
                        tables = classes.filter {
                            it.partOfDatabaseAsType(database.classType, ClassModel.ClassType.Normal)
                        },
                        views = classes.filter {
                            it.partOfDatabaseAsType(database.classType, ClassModel.ClassType.View)
                        },
                        queryModels = classes.filter {
                            it.partOfDatabaseAsType(database.classType, ClassModel.ClassType.Query)
                        }
                    )
                }

            val holderModel = DatabaseHolderModel(
                name = NameModel(ClassNames.GeneratedDatabaseHolder),
                databases,
                properties = DatabaseHolderProperties("")
            )

            referencesCache.allTables = classes

            objects.filterIsInstance<TypeConverterModel>().forEach { model ->
                typeConverterCache.putTypeConverter(model)
            }


            listOf(
                classes.map(classWriter::create),
                databases.map(databaseWriter::create),
                listOf(holderModel).map(databaseHolderWriter::create)
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