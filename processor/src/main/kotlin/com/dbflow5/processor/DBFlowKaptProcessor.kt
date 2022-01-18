package com.dbflow5.processor

import com.dbflow5.codegen.shared.Annotations
import com.dbflow5.codegen.shared.ClassModel
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
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.processor.interop.KaptResolver
import com.dbflow5.processor.parser.KaptElementProcessor
import com.grosner.dbflow5.codegen.kotlin.writer.ClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseHolderWriter
import com.grosner.dbflow5.codegen.kotlin.writer.DatabaseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.InlineTypeConverterWriter
import com.grosner.dbflow5.codegen.kotlin.writer.ManyToManyClassWriter
import com.grosner.dbflow5.codegen.kotlin.writer.OneToManyClassWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * Description:
 */
class DBFlowKaptProcessor(
    private val elements: Elements,
    private val filer: Filer,
    private val typeConverterCache: TypeConverterCache,
    private val messager: Messager,
    private val kaptElementProcessor: KaptElementProcessor,
    private val classWriter: ClassWriter,
    private val databaseWriter: DatabaseWriter,
    private val databaseHolderWriter: DatabaseHolderWriter,
    private val referencesCache: ReferencesCache,
    private val manyClassWriter: ManyToManyClassWriter,
    private val oneToManyClassWriter: OneToManyClassWriter,
    private val typeConverterWriter: InlineTypeConverterWriter,
) {

    fun process(roundEnvironment: RoundEnvironment) {
        try {
            val kaptResolver = KaptResolver(elements)
            typeConverterCache.applyResolver(kaptResolver)

            val elements = Annotations.values.map {
                roundEnvironment.getElementsAnnotatedWith(
                    elements.getTypeElement(it.qualifiedName)
                )
            }.flatten()
                .distinct()
            if (elements.isNotEmpty()) {
                val objects = elements.mapNotNull { element ->
                    when (element) {
                        is TypeElement -> {
                            kaptElementProcessor.parse(element)
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
                        typeConverterCache.putTypeConverter(typeConverterName, kaptResolver)
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
                        it.writeTo(filer)
                    }
            }
        } catch (exception: ValidationException) {
            messager.printMessage(Diagnostic.Kind.ERROR, exception.localizedMessage)
        } catch (e: Throwable) {
            e.printStackTrace()
            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }
    }
}