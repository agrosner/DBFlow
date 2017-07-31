package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.TypeConverter
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.processor.definition.DatabaseHolderDefinition
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class DBFlowProcessor : AbstractProcessor() {

    private lateinit var manager: ProcessorManager

    /**
     * If the processor class is annotated with [ ], return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.

     * @return the names of the annotation types supported by this
     * * processor, or an empty set if none
     */
    override fun getSupportedAnnotationTypes()
        = linkedSetOf(Table::class.java.canonicalName,
        Column::class.java.canonicalName,
        TypeConverter::class.java.canonicalName,
        ModelView::class.java.canonicalName,
        Migration::class.java.canonicalName,
        ContentProvider::class.java.canonicalName,
        TableEndpoint::class.java.canonicalName,
        ColumnIgnore::class.java.canonicalName,
        QueryModel::class.java.canonicalName
    )

    override fun getSupportedOptions() = linkedSetOf(DatabaseHolderDefinition.OPTION_TARGET_MODULE_NAME)

    /**
     * If the processor class is annotated with [ ], return the source version in the
     * annotation.  If the class is not so annotated, [ ][javax.lang.model.SourceVersion.RELEASE_6] is returned.

     * @return the latest source version supported by this processor
     */
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        manager = ProcessorManager(processingEnv)
        manager.addHandlers(MigrationHandler(),
            TypeConverterHandler(),
            DatabaseHandler(),
            TableHandler(),
            QueryModelHandler(),
            ModelViewHandler(),
            ContentProviderHandler(),
            TableEndpointHandler())
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        manager.handle(manager, roundEnv)

        // return true if we successfully processed the Annotation.
        return true
    }

}
