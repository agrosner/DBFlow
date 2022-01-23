package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseHolderModel
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.MigrationModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.ObjectModel
import com.dbflow5.codegen.shared.OneToManyModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.copyOverClasses
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.properties.DatabaseHolderProperties
import com.squareup.kotlinpoet.FileSpec

/**
 * Description:
 */
class ObjectWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
    private val classWriter: ClassWriter,
    private val databaseWriter: DatabaseWriter,
    private val databaseHolderWriter: DatabaseHolderWriter,
    private val manyClassWriter: ManyToManyClassWriter,
    private val oneToManyClassWriter: OneToManyClassWriter,
    private val typeConverterWriter: InlineTypeConverterWriter,
) {

    fun write(
        resolver: ClassNameResolver,
        objects: List<ObjectModel>,
        writerFn: (FileSpec) -> Unit
    ) {
        typeConverterCache.applyResolver(resolver)

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
            .onEach { db ->
                println("Found DB:${db.name} ${db.properties.tables} : ${db.tables.map { it.name }}")
            }
        classes.forEach { clazz ->
            println("Found Classes:${clazz.name}:${clazz.classType} from DB: ${clazz.properties.database}")
        }

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
            .forEach { writerFn(it) }
    }
}