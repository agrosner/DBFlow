package com.dbflow5.processor

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.QueryModel
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.converter.BigDecimalConverter
import com.dbflow5.converter.BigIntegerConverter
import com.dbflow5.converter.BooleanConverter
import com.dbflow5.converter.CalendarConverter
import com.dbflow5.converter.CharConverter
import com.dbflow5.converter.DateConverter
import com.dbflow5.converter.SqlDateConverter
import com.dbflow5.converter.UUIDConverter
import com.dbflow5.processor.definition.DatabaseDefinition
import com.dbflow5.processor.definition.ManyToManyDefinition
import com.dbflow5.processor.definition.MigrationDefinition
import com.dbflow5.processor.definition.ModelViewDefinition
import com.dbflow5.processor.definition.QueryModelDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.fromTypeMirror
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

/**
 * Description: The main base-level handler for performing some action when the
 * [DBFlowProcessor.process] is called.
 */
interface Handler {

    /**
     * Called when the process of the [DBFlowProcessor] is called

     * @param processorManager The manager that holds processing information
     * *
     * @param roundEnvironment The round environment
     */
    fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment)
}

/**
 * Description: The base handler than provides common callbacks into processing annotated top-level elements
 */
abstract class AnnotatedHandler<AnnotationClass : Annotation>(private val annotationClass: KClass<AnnotationClass>) :
    Handler {

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        val annotatedElements =
            roundEnvironment.getElementsAnnotatedWith(annotationClass.java).toMutableSet()
        processElements(processorManager, annotatedElements)
        if (annotatedElements.size > 0) {
            annotatedElements.forEach { element ->
                element.getAnnotation(annotationClass.java)?.let { annotation ->
                    onProcessElement(annotation, element, processorManager)
                }
            }
            afterProcessElements(processorManager)
        }
    }

    open fun processElements(
        processorManager: ProcessorManager,
        annotatedElements: MutableSet<Element>
    ) {

    }

    protected abstract fun onProcessElement(
        annotation: AnnotationClass,
        element: Element,
        processorManager: ProcessorManager
    )

    open fun afterProcessElements(processorManager: ProcessorManager) {

    }
}

/**
 * Description: Handles [Migration] by creating [MigrationDefinition]
 * and adds them to the [ProcessorManager]
 */
class MigrationHandler : AnnotatedHandler<Migration>(Migration::class) {

    override fun onProcessElement(
        annotation: Migration,
        element: Element,
        processorManager: ProcessorManager
    ) {
        if (element is TypeElement) {
            element.annotation<Migration>()?.let { migration ->
                val migrationDefinition = MigrationDefinition(migration, processorManager, element)
                processorManager.addMigrationDefinition(migrationDefinition)
            }
        }
    }
}

/**
 * Description: Handles [ModelView] annotations, writing
 * ModelViewAdapters, and adding them to the [ProcessorManager]
 */
class ModelViewHandler : AnnotatedHandler<ModelView>(ModelView::class) {

    override fun onProcessElement(
        annotation: ModelView,
        element: Element,
        processorManager: ProcessorManager
    ) {
        if (element is TypeElement) {
            element.annotation<ModelView>()?.let { modelView ->
                val modelViewDefinition = ModelViewDefinition(modelView, processorManager, element)
                processorManager.addModelViewDefinition(modelViewDefinition)
            }
        }
    }
}

/**
 * Description: Handles [QueryModel] annotations, writing QueryModelAdapter, and
 * adding them to the [ProcessorManager].
 */
class QueryModelHandler : AnnotatedHandler<Query>(Query::class) {

    override fun onProcessElement(
        annotation: Query,
        element: Element,
        processorManager: ProcessorManager
    ) {
        if (element is TypeElement) {
            element.annotation<Query>()?.let { queryModel ->
                val queryModelDefinition =
                    QueryModelDefinition(queryModel, element, processorManager)
                processorManager.addQueryModelDefinition(queryModelDefinition)
            }
        }
    }
}

/**
 * Description: Handles [Table] annotations, writing ModelAdapters,
 * and adding them to the [ProcessorManager]
 */
class TableHandler : AnnotatedHandler<Table>(Table::class) {

    override fun onProcessElement(
        annotation: Table,
        element: Element,
        processorManager: ProcessorManager
    ) {
        if (element is TypeElement) {
            val tableDefinition = TableDefinition(annotation, processorManager, element)
            processorManager.addTableDefinition(tableDefinition)

            element.annotation<ManyToMany>()?.let { manyToMany ->
                val manyToManyDefinition =
                    ManyToManyDefinition(element, processorManager, manyToMany)
                processorManager.addManyToManyDefinition(manyToManyDefinition)
            }

            if (element.annotation<MultipleManyToMany>() != null) {
                val multipleManyToMany = element.annotation<MultipleManyToMany>()
                multipleManyToMany?.value?.forEach {
                    processorManager.addManyToManyDefinition(
                        ManyToManyDefinition(
                            element,
                            processorManager,
                            it
                        )
                    )
                }
            }
        }
    }
}

/**
 * Description: Handles [TypeConverter] annotations,
 * adding default methods and adding them to the [ProcessorManager]
 */
class TypeConverterHandler : AnnotatedHandler<TypeConverter>(TypeConverter::class) {

    private var typeConverterElements = setOf<Element>()
    private val typeConverterDefinitions = mutableSetOf<TypeConverterDefinition>()
    override fun processElements(
        processorManager: ProcessorManager,
        annotatedElements: MutableSet<Element>
    ) {
        typeConverterElements = DEFAULT_TYPE_CONVERTERS.mapTo(mutableSetOf()) {
            processorManager.elements.getTypeElement(it.name)
        }
        annotatedElements.addAll(typeConverterElements)
    }

    override fun onProcessElement(
        annotation: TypeConverter,
        element: Element,
        processorManager: ProcessorManager
    ) {
        if (element is TypeElement) {
            fromTypeMirror(element.asType(), processorManager)?.let { className ->
                val definition = TypeConverterDefinition(
                    annotation, className, element.asType(), processorManager,
                    isDefaultConverter = typeConverterElements.contains(element)
                )
                if (VALIDATOR.validate(processorManager, definition)) {
                    // allow user overrides from default.
                    // Check here if user already placed definition of same type, since default converters
                    // are added last.
                    if (processorManager.typeConverters
                            .filter { it.value.modelTypeName == definition.modelTypeName }
                            .isEmpty()
                    ) {
                        typeConverterDefinitions.add(definition)
                    }
                }
            }
        }
    }

    override fun afterProcessElements(processorManager: ProcessorManager) {
        // validate multiple global registered do not exist.
        val grouping = typeConverterDefinitions
            .filter { it.isDefaultConverter }
            .groupingBy { it.modelTypeName }
        val groupingMap =
            grouping.aggregate { key, accumulator: MutableSet<TypeConverterDefinition>?, element: TypeConverterDefinition, first: Boolean ->
                val set = accumulator ?: mutableSetOf()
                set.add(element)
                return@aggregate set
            }
        grouping.eachCount()
            .forEach { (type, count) ->
                if (count > 1) {
                    processorManager.logError(TypeConverterHandler::class,
                        "Multiple registered @TypeConverter of type $type found. " +
                            "Pick one for global and make the other a local converter for a @Column. " +
                            "Or explicitly specify both as field converters." +
                            "\n${
                                groupingMap[type]?.joinToString("\n") { " ${it.className} registered for ${it.modelTypeName} <-> ${it.dbTypeName}" }
                            }\n")
                }
            }
        // sort default converters first so that they can get overwritten
        typeConverterDefinitions
            .sortedBy { !it.isDefaultConverter }
            .forEach { def -> processorManager.addTypeConverterDefinition(def) }
    }

    companion object {
        private val VALIDATOR = TypeConverterValidator()
        private val DEFAULT_TYPE_CONVERTERS = arrayOf<Class<*>>(
            CalendarConverter::class.java,
            BigDecimalConverter::class.java, BigIntegerConverter::class.java,
            DateConverter::class.java, SqlDateConverter::class.java,
            BooleanConverter::class.java, UUIDConverter::class.java,
            CharConverter::class.java
        )
    }
}

/**
 * Description: Deals with writing database definitions
 */
class DatabaseHandler : AnnotatedHandler<Database>(Database::class) {

    private val validator = DatabaseValidator()

    override fun onProcessElement(
        annotation: Database,
        element: Element,
        processorManager: ProcessorManager
    ) {
        val managerWriter = DatabaseDefinition(annotation, processorManager, element)
        if (validator.validate(processorManager, managerWriter)) {
            processorManager.addDatabaseDefinition(managerWriter)
        }
    }

    companion object {
        val TYPE_CONVERTER_MAP_FIELD_NAME = "typeConverters"
        val MODEL_ADAPTER_MAP_FIELD_NAME = "modelAdapters"
        val QUERY_MODEL_ADAPTER_MAP_FIELD_NAME = "queryModelAdapterMap"
        val MIGRATION_FIELD_NAME = "migrationMap"
        val MODEL_VIEW_ADAPTER_MAP_FIELD_NAME = "modelViewAdapterMap"
        val MODEL_NAME_MAP = "modelTableNames"
    }
}