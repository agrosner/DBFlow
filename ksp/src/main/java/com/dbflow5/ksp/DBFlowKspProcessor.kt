package com.dbflow5.ksp

import com.dbflow5.codegen.shared.Annotations
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseHolderModel
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.MigrationModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.OneToManyModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.copyOverClasses
import com.dbflow5.codegen.shared.properties.DatabaseHolderProperties
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.ksp.model.interop.KSPResolver
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.grosner.dbflow5.codegen.kotlin.writer.ClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseHolderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.InlineTypeConverterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ManyToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.OneToManyClassWriter
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
        try {
            val kspResolver = KSPResolver(resolver)
            typeConverterCache.applyResolver(kspResolver)
            ksClassDeclarationParser.applyResolver(resolver)

            val symbols =
                Annotations.values.map {
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
                    .map(copyOverClasses(classes, migrations))

                val holderModel = DatabaseHolderModel(
                    name = NameModel(ClassNames.GeneratedDatabaseHolder),
                    databases,
                    properties = DatabaseHolderProperties(""),
                    allOriginatingFiles = objects.mapNotNull { it.originatingSource }
                )

                referencesCache.allClasses = classes

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
                        typeConverterCache.putTypeConverter(typeConverterName, kspResolver)
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
                        it.writeTo(
                            environment.codeGenerator,
                            aggregating = false,
                        )
                    }
            }
        } catch (exception: ValidationException) {
            environment.logger.exception(exception)
        }

        return listOf()
    }
}