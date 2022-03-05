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
import com.dbflow5.codegen.shared.interop.OriginatingSourceCollection
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.properties.DatabaseHolderProperties
import com.dbflow5.codegen.shared.validation.ObjectValidatorMap
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.asClassName

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
    private val objectValidatorMap: ObjectValidatorMap,
    private val fieldSanitizer: FieldSanitizer,
    private val nameAllocator: NameAllocator,
) {

    fun write(
        resolver: ClassNameResolver,
        objects: List<ObjectModel>,
        writerFn: (FileSpec) -> Unit
    ) {
        fieldSanitizer.applyResolver(resolver)
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
            .onEach { model ->
                // prep names for use
                nameAllocator.newName(
                    suggestion = model.generatedFieldName,
                    tag = model.generatedClassName)
            }
        val migrations = objects.filterIsInstance<MigrationModel>()

        // associate classes into DB.
        val databases = objects.filterIsInstance<DatabaseModel>()
            .map(copyOverClasses(classes, migrations))

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
                if (typeConverterName != Any::class.asClassName()) {
                    typeConverterCache.putTypeConverter(typeConverterName, resolver)
                }
            }

        val holderModel = DatabaseHolderModel(
            name = NameModel(ClassNames.GeneratedDatabaseHolderFactory),
            databases = databases,
            tables = classes.filter { it.isNormal },
            queries = classes.filter { it.isQuery },
            views = classes.filter { it.isView },
            properties = DatabaseHolderProperties(""),
            originatingSource = OriginatingSourceCollection(objects.mapNotNull { it.originatingSource })
        )

        listOf(
            typeConverterCache.generatedTypeConverters,
            objects,
            listOf(holderModel),
        )
            .flatten()
            .forEach { objectValidatorMap.validate(it) }

        // order is important of this check. TypeConverter validator
        // ensures we don't produce a reference cycle of types.
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
                writerFn(it)
            }
    }
}