package com.raizlabs.android.dbflow.processor

import com.google.common.collect.Sets
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.annotation.TypeConverter
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.converter.*
import com.raizlabs.android.dbflow.processor.definition.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
/**
 * Description: The main base-level handler for performing some action when the
 * [Processor.process] is called.
 */
interface Handler {

    /**
     * Called when the process of the [Processor] is called

     * @param processorManager The manager that holds processing information
     * *
     * @param roundEnvironment The round environment
     */
    fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment)
}

/**
 * Description: Handles [com.raizlabs.android.dbflow.annotation.Migration] by creating [com.raizlabs.android.dbflow.processor.definition.MigrationDefinition]
 * and adds them to the [com.raizlabs.android.dbflow.processor.model.ProcessorManager]
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

/**
 * Description:
 */
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
 * Description: Handles [com.raizlabs.android.dbflow.annotation.Table] annotations, writing ModelAdapters,
 * and adding them to the [com.raizlabs.android.dbflow.processor.model.ProcessorManager]
 */
class TableHandler : BaseContainerHandler<Table>() {

    override val annotationClass = Table::class.java

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        if (element is TypeElement && element.getAnnotation(annotationClass) != null) {
            val tableDefinition = TableDefinition(processorManager, element)
            processorManager.addTableDefinition(tableDefinition)

            if (element.getAnnotation(ManyToMany::class.java) != null) {
                val manyToManyDefinition = ManyToManyDefinition(element, processorManager)
                processorManager.addManyToManyDefinition(manyToManyDefinition)
            }

            if (element.getAnnotation(MultipleManyToMany::class.java) != null) {
                val multipleManyToMany = element.getAnnotation(MultipleManyToMany::class.java)
                multipleManyToMany.value.forEach {
                    processorManager.addManyToManyDefinition(ManyToManyDefinition(element, processorManager, it))
                }
            }
        }
    }
}

/**
 * Description: Handles [com.raizlabs.android.dbflow.annotation.TypeConverter] annotations,
 * adding default methods and adding them to the [com.raizlabs.android.dbflow.processor.model.ProcessorManager]
 */
class TypeConverterHandler : BaseContainerHandler<TypeConverter>() {

    override val annotationClass = TypeConverter::class.java

    override fun processElements(processorManager: ProcessorManager, annotatedElements: MutableSet<Element>) {
        for (clazz in DEFAULT_TYPE_CONVERTERS) {
            annotatedElements.add(processorManager.elements.getTypeElement(clazz.getName()))
        }
    }

    override fun onProcessElement(processorManager: ProcessorManager, element: Element) {
        if (element is TypeElement) {
            val className = ProcessorUtils.fromTypeMirror(element.asType(), processorManager)
            val converterDefinition = className?.let { TypeConverterDefinition(it, element.asType(), processorManager) }
            converterDefinition?.let {
                if (VALIDATOR.validate(processorManager, converterDefinition)) {
                    processorManager.addTypeConverterDefinition(converterDefinition)
                }
            }
        }
    }

    companion object {
        private val VALIDATOR = TypeConverterValidator()
        private val DEFAULT_TYPE_CONVERTERS = arrayOf<Class<*>>(CalendarConverter::class.java,
                BigDecimalConverter::class.java,
                DateConverter::class.java, SqlDateConverter::class.java,
                BooleanConverter::class.java, UUIDConverter::class.java)
    }
}

/**
 * Description: The base handler than provides common callbacks into processing annotated top-level elements
 */
abstract class BaseContainerHandler<AnnotationClass : Annotation> : Handler {

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        val annotatedElements = Sets.newHashSet(roundEnvironment.getElementsAnnotatedWith(annotationClass))
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

/**
 * Description:
 */
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
        val METHOD_MODIFIERS: Set<Modifier> = Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL)
        val MODEL_FIELD_NAME = "models"
        val MODEL_ADAPTER_MAP_FIELD_NAME = "modelAdapters"
        val QUERY_MODEL_ADAPTER_MAP_FIELD_NAME = "queryModelAdapterMap"
        val MODEL_VIEW_FIELD_NAME = "modelViews"
        val MIGRATION_FIELD_NAME = "migrationMap"
        val MODEL_VIEW_ADAPTER_MAP_FIELD_NAME = "modelViewAdapterMap"
        val MODEL_NAME_MAP = "modelTableNames"
    }
}