package com.dbflow5.processor

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.processor.definition.DatabaseHolderDefinition
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
    override fun getSupportedAnnotationTypes() = listOf(
        Table::class,
        Column::class,
        TypeConverter::class,
        ModelView::class,
        Migration::class,
        ColumnIgnore::class,
        Query::class,
        Fts3::class,
        Fts4::class,
        MultipleManyToMany::class
    ).mapTo(linkedSetOf<String>()) { it.java.canonicalName }

    override fun getSupportedOptions() =
        linkedSetOf(DatabaseHolderDefinition.OPTION_TARGET_MODULE_NAME)

    /**
     * If the processor class is annotated with [ ], return the source version in the
     * annotation.  If the class is not so annotated, [ ][javax.lang.model.SourceVersion.RELEASE_6] is returned.

     * @return the latest source version supported by this processor
     */
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        manager = ProcessorManager(processingEnv)
        manager.addHandlers(
            MigrationHandler(),
            TypeConverterHandler(),
            DatabaseHandler(),
            TableHandler(),
            QueryModelHandler(),
            ModelViewHandler(),
        )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        manager.handle(manager, roundEnv)

        // return true if we successfully processed the Annotation.
        return true
    }

}
