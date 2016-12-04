package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.processor.ProcessorManager.Companion.manager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.squareup.javapoet.ClassName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Description: Provides handy methods for processing
 */
object ProcessorUtils {

    /**
     * Whether the specified element is assignable to the fqTn parameter

     * @param processingEnvironment The environment this runs in
     * *
     * @param fqTn                  THe fully qualified type name of the element we want to check
     * *
     * @param element               The element to check that implements
     * *
     * @return true if element implements the fqTn
     */
    fun implementsClass(processingEnvironment: ProcessingEnvironment, fqTn: String, element: TypeElement?): Boolean {
        val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
        if (typeElement == null) {
            processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: " + fqTn + "" +
                "ensure that the visibility of the class is not private.")
            return false
        } else {
            var classMirror: TypeMirror? = typeElement.asType()
            if (classMirror != null) {
                classMirror = processingEnvironment.typeUtils.erasure(classMirror)
            }
            return classMirror != null && element != null && element.asType() != null &&
                processingEnvironment.typeUtils.isAssignable(element.asType(), classMirror)
        }
    }

    /**
     * Whether the specified element is assignable to the fqTn parameter

     * @param processingEnvironment The environment this runs in
     * *
     * @param fqTn                  THe fully qualified type name of the element we want to check
     * *
     * @param element               The element to check that implements
     * *
     * @return true if element implements the fqTn
     */
    fun isSubclass(processingEnvironment: ProcessingEnvironment, fqTn: String, element: TypeElement?): Boolean {
        val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
        if (typeElement == null) {
            processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: " + fqTn + "" +
                "ensure that the visibility of the class is not private.")
            return false
        } else {
            val classMirror = typeElement.asType()
            return classMirror != null && element != null && element.asType() != null && processingEnvironment.typeUtils.isSubtype(element.asType(), classMirror)
        }
    }

    fun fromTypeMirror(typeMirror: TypeMirror, processorManager: ProcessorManager): ClassName? {
        var className: ClassName? = null
        val element = getTypeElement(typeMirror)
        if (element != null) {
            className = ClassName.get(element)
        } else {
            className = ElementUtility.getClassName(typeMirror.toString(), processorManager)
        }
        return className
    }

    fun getTypeElement(element: Element): TypeElement? {
        val typeElement: TypeElement?
        if (element is TypeElement) {
            typeElement = element
        } else {
            typeElement = getTypeElement(element.asType())
        }
        return typeElement
    }

    fun getTypeElement(typeMirror: TypeMirror): TypeElement? {
        val manager = ProcessorManager.manager
        var typeElement: TypeElement? = manager.elements.getTypeElement(typeMirror.toString())
        if (typeElement == null) {
            val el = manager.typeUtils.asElement(typeMirror)
            typeElement = if (el != null) (el as TypeElement) else null
        }
        return typeElement
    }

    fun ensureVisibleStatic(element: Element, typeElement: TypeElement,
                            name: String) {
        if (element.modifiers.contains(Modifier.PRIVATE)
            || element.modifiers.contains(Modifier.PROTECTED)) {
            manager.logError("$name must be visible from: " + typeElement)
        }
        if (!element.modifiers.contains(Modifier.STATIC)) {
            manager.logError("$name must be static from: " + typeElement)
        }

        if (!element.modifiers.contains(Modifier.FINAL)) {
            manager.logError("The $name must be final")
        }
    }
}
