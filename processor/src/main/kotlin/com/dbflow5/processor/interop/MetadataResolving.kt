package com.dbflow5.processor.interop

import com.dbflow5.processor.ProcessorManager
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toKmClass
import kotlinx.metadata.KmClass
import javax.lang.model.element.TypeElement

/**
 * Checks if we can resolve into a [KmClass] otherwise then call [fallback].
 */
fun <T> safeResolveMetaData(
    typeElement: TypeElement,
    fallback: () -> T,
    metadata: (
        typeSpec: TypeSpec, kmClass: KmClass,
    ) -> T
): T {
    try {
        val kmClass = typeElement.toKmClass()
        val typeSpec = kmClass.toTypeSpec(
            ElementsClassInspector.create(
                ProcessorManager.manager.elements,
                ProcessorManager.manager.typeUtils,
            )
        )
        return metadata(typeSpec, kmClass)
    } catch (e: Throwable) {
        // log error
    }
    // no metadata found for kotlin, fall back on java
    return fallback()
}