package com.raizlabs.android.dbflow.processor

import com.google.auto.service.AutoService
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.processor.handler.*
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Author: andrewgrosner
 */
@AutoService(Processor::class)
class DBFlowProcessor : AbstractProcessor() {

    private lateinit var manager: ProcessorManager

    /**
     * If the processor class is annotated with [ ], return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.

     * @return the names of the annotation types supported by this
     * * processor, or an empty set if none
     */
    override fun getSupportedAnnotationTypes(): Set<String> {
        return linkedSetOf(Table::class.java.canonicalName, Column::class.java.canonicalName,
                TypeConverter::class.java.canonicalName, ModelView::class.java.canonicalName,
                Migration::class.java.canonicalName, ContentProvider::class.java.canonicalName,
                TableEndpoint::class.java.canonicalName, ColumnIgnore::class.java.canonicalName,
                QueryModel::class.java.canonicalName)
    }

    /**
     * If the processor class is annotated with [ ], return the source version in the
     * annotation.  If the class is not so annotated, [ ][javax.lang.model.SourceVersion.RELEASE_6] is returned.

     * @return the latest source version supported by this processor
     */
    override fun getSupportedSourceVersion(): SourceVersion? = SourceVersion.latestSupported()

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        manager = ProcessorManager(processingEnv).apply {
            addHandlers(MigrationHandler(), TypeConverterHandler(), DatabaseHandler(),
                    TableHandler(), QueryModelHandler(), ModelViewHandler(), ContentProviderHandler(),
                    TableEndpointHandler())
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        manager.handle(manager, roundEnv)

        // return true if we successfully processed the Annotation.
        return true
    }

}
