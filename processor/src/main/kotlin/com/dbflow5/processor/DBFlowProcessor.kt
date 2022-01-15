package com.dbflow5.processor

import com.dbflow5.codegen.model.Annotations
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
    override fun getSupportedAnnotationTypes() =
        Annotations.values.mapTo(linkedSetOf()) { it.qualifiedName }

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
