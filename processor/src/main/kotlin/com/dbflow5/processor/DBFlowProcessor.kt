package com.dbflow5.processor

import com.dbflow5.codegen.shared.Annotations
import com.dbflow5.codegen.shared.sharedModule
import com.grosner.dbflow5.codegen.kotlin.codeGenModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class DBFlowProcessor : AbstractProcessor(), KoinComponent {

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
        startKoin {
            modules(
                sharedModule,
                codeGenModule,
                getModule(processingEnv)
            )
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        inject<DBFlowKaptProcessor>().value.process(roundEnv)

        // return true if we successfully processed the Annotation.
        return true
    }

}
