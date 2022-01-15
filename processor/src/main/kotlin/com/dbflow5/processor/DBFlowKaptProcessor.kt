package com.dbflow5.processor

import com.dbflow5.codegen.model.Annotations
import com.dbflow5.codegen.model.cache.TypeConverterCache
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.processor.interop.KaptResolver
import com.dbflow5.processor.parser.KaptElementProcessor
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
    private val typeConverterCache: TypeConverterCache,
    private val messager: Messager,
    private val kaptElementProcessor: KaptElementProcessor,
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
                }

                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Found objects ${objects}"
                )
            }
        } catch (exception: ValidationException) {
            messager.printMessage(Diagnostic.Kind.ERROR, exception.localizedMessage)
        } catch (e: Throwable) {
            e.printStackTrace()
            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }
    }
}