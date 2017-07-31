package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.MultipleManyToMany
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.TypeConverter
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.converter.BigDecimalConverter
import com.raizlabs.android.dbflow.converter.BigIntegerConverter
import com.raizlabs.android.dbflow.converter.BooleanConverter
import com.raizlabs.android.dbflow.converter.CalendarConverter
import com.raizlabs.android.dbflow.converter.CharConverter
import com.raizlabs.android.dbflow.converter.DateConverter
import com.raizlabs.android.dbflow.converter.SqlDateConverter
import com.raizlabs.android.dbflow.converter.UUIDConverter
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.definition.DatabaseDefinition
import com.raizlabs.android.dbflow.processor.definition.ManyToManyDefinition
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.utils.annotation
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

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
 * Description: Handles [Migration] by creating [MigrationDefinition]
 * and adds them to the [ProcessorManager]
 */
class MigrationHandler : BaseContainerHandler<Migration>() {

    override val annotationClass = Migration::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        if (element is TypeElement) {
            val migrationDefinition = MigrationDefinition(processorManager, element)
            processorManager.addMigrationDefinition(migrationDefinition)
        }
    }
}

/**
 * Description: Handles [ModelView] annotations, writing
 * ModelViewAdapters, and adding them to the [ProcessorManager]
 */
class ModelViewHandler : BaseContainerHandler<ModelView>() {

    override val annotationClass = ModelView::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        val modelViewDefinition = ModelViewDefinition(processorManager, element)
        processorManager.addModelViewDefinition(modelViewDefinition)
    }
}

/**
 * Description: Handles [QueryModel] annotations, writing QueryModelAdapter, and
 * adding them to the [ProcessorManager].
 */
class QueryModelHandler : BaseContainerHandler<QueryModel>() {

    override val annotationClass = QueryModel::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        val queryModelDefinition = QueryModelDefinition(element, processorManager)
        if (queryModelDefinition.databaseTypeName != null) {
            processorManager.addQueryModelDefinition(queryModelDefinition)
        }
    }
}

class TableEndpointHandler : BaseContainerHandler<TableEndpoint>() {

    private val validator: TableEndpointValidator = TableEndpointValidator()

    override val annotationClass = TableEndpoint::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {

        // top-level only
        if (element.enclosingElement is PackageElement) {
            val tableEndpointDefinition = TableEndpointDefinition(element, processorManager)
            if (validator.validate(processorManager, tableEndpointDefinition)) {
                processorManager.putTableEndpointForProvider(tableEndpointDefinition)
            }
        }
    }
}

/**
 * Description: Handles [Table] annotations, writing ModelAdapters,
 * and adding them to the [ProcessorManager]
 */
class TableHandler : BaseContainerHandler<Table>() {

    override val annotationClass = Table::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        if (element is TypeElement && element.getAnnotation(annotationClass) != null) {
            val tableDefinition = TableDefinition(processorManager, element)
            processorManager.addTableDefinition(tableDefinition)

            if (element.annotation<ManyToMany>() != null) {
                val manyToManyDefinition = ManyToManyDefinition(element, processorManager)
                processorManager.addManyToManyDefinition(manyToManyDefinition)
            }

            if (element.annotation<MultipleManyToMany>() != null) {
                val multipleManyToMany = element.annotation<MultipleManyToMany>()
                multipleManyToMany?.value?.forEach {
                    processorManager.addManyToManyDefinition(ManyToManyDefinition(element, processorManager, it))
                }
            }
        }
    }
}

/**
 * Description: Handles [TypeConverter] annotations,
 * adding default methods and adding them to the [ProcessorManager]
 */
class TypeConverterHandler : BaseContainerHandler<TypeConverter>() {

    override val annotationClass = TypeConverter::class.java

    override fun processElements(processorManager: ProcessorManager, annotatedElements: MutableSet<Element>) {
        DEFAULT_TYPE_CONVERTERS.mapTo(annotatedElements) { processorManager.elements.getTypeElement(it.name) }
    }

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        if (element is TypeElement) {
            val className = com.raizlabs.android.dbflow.processor.utils.fromTypeMirror(element.asType(), processorManager)
            val converterDefinition = className?.let { TypeConverterDefinition(it, element.asType(), processorManager, element) }
            converterDefinition?.let {
                if (VALIDATOR.validate(processorManager, converterDefinition)) {
                    // allow user overrides from default.
                    // Check here if user already placed definition of same type, since default converters
                    // are added last.
                    if (processorManager.typeConverters
                        .filter { it.value.modelTypeName == converterDefinition.modelTypeName }
                        .isEmpty()) {
                        processorManager.addTypeConverterDefinition(converterDefinition)
                    }
                }
            }
        }
    }

    companion object {
        private val VALIDATOR = TypeConverterValidator()
        private val DEFAULT_TYPE_CONVERTERS = arrayOf<Class<*>>(CalendarConverter::class.java,
            BigDecimalConverter::class.java, BigIntegerConverter::class.java,
            DateConverter::class.java, SqlDateConverter::class.java,
            BooleanConverter::class.java, UUIDConverter::class.java,
            CharConverter::class.java)
    }
}

/**
 * Description: The base handler than provides common callbacks into processing annotated top-level elements
 */
abstract class BaseContainerHandler<AnnotationClass : Annotation> : Handler {

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotationClass).toMutableSet()
        processElements(processorManager, annotatedElements)
        if (annotatedElements.size > 0) {
            annotatedElements.forEach { onProcessElement(processorManager, it) }
        }
    }

    protected abstract val annotationClass: Class<AnnotationClass>

    open fun processElements(processorManager: ProcessorManager, annotatedElements: MutableSet<Element>) {

    }

    protected abstract fun onProcessElement(processorManager: ProcessorManager, element: Element)
}

class ContentProviderHandler : BaseContainerHandler<ContentProvider>() {

    override val annotationClass = ContentProvider::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        val contentProviderDefinition = ContentProviderDefinition(element, processorManager)
        if (contentProviderDefinition.elementClassName != null) {
            processorManager.addContentProviderDefinition(contentProviderDefinition)
        }
    }
}

/**
 * Description: Deals with writing database definitions
 */
class DatabaseHandler : BaseContainerHandler<Database>() {

    private val validator = DatabaseValidator()

    override val annotationClass = Database::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        val managerWriter = DatabaseDefinition(processorManager, element)
        if (validator.validate(processorManager, managerWriter)) {
            processorManager.addFlowManagerWriter(managerWriter)
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