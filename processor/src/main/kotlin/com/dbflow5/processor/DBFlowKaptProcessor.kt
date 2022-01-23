package com.dbflow5.processor

import com.dbflow5.codegen.shared.Annotations
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.processor.interop.KaptResolver
import com.dbflow5.processor.parser.KaptElementProcessor
import com.grosner.dbflow5.codegen.kotlin.writer.ObjectWriter
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
    private val messager: Messager,
    private val kaptElementProcessor: KaptElementProcessor,
    private val objectWriter: ObjectWriter,
) {

    fun process(roundEnvironment: RoundEnvironment) {
        try {
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

                objectWriter.write(
                    KaptResolver(this.elements),
                    objects
                ) {
                    try {
                        println("Writing file ${it.name}")
                        it.writeTo(filer)
                    } catch (e: Throwable) {
                        messager.printMessage(Diagnostic.Kind.WARNING, e.message)
                    }
                }
            }
        } catch (exception: ValidationException) {
            messager.printMessage(Diagnostic.Kind.ERROR, exception.localizedMessage)
        } catch (e: Throwable) {
            messager.printMessage(Diagnostic.Kind.WARNING, e.message)
        }
    }
}