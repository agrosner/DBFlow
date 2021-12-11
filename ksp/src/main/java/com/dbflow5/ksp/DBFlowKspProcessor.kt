package com.dbflow5.ksp

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.ReferencesCache
import com.dbflow5.ksp.model.partOfDatabaseAsType
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.dbflow5.ksp.writer.ClassWriter
import com.dbflow5.ksp.writer.DatabaseWriter
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Description:
 */
class DBFlowKspProcessor(
    private val ksClassDeclarationParser: KSClassDeclarationParser,
    private val classWriter: ClassWriter,
    private val databaseWriter: DatabaseWriter,
    private val referencesCache: ReferencesCache,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            Annotations.values().map {
                resolver.getSymbolsWithAnnotation(
                    it.qualifiedName
                ).toList()
            }.flatten()
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

        referencesCache.allTables = classes

        println("Objects $objects")

        listOf(
            classes.map(classWriter::create),
            databases.map(databaseWriter::create)
        )
            .flatten()
            .forEach { it.writeTo(System.out) }

        return listOf()
    }
}