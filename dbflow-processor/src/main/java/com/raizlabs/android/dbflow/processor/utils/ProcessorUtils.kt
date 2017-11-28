package com.raizlabs.android.dbflow.processor.utils

import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.ProcessorManager.Companion.manager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Whether the specified element implements the [ClassName]
 */
fun TypeElement?.implementsClass(processingEnvironment: ProcessingEnvironment
                                 = manager.processingEnvironment, className: ClassName)
        = implementsClass(processingEnvironment, className.toString())

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
fun TypeElement?.implementsClass(processingEnvironment: ProcessingEnvironment, fqTn: String): Boolean {
    val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
    if (typeElement == null) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR,
                "Type Element was null for: $fqTn ensure that the visibility of the class is not private.")
        return false
    } else {
        val classMirror: TypeMirror? = typeElement.asType().erasure()
        if (classMirror == null || this?.asType() == null) {
            return false
        }
        val elementType = this.asType()
        return elementType != null && (processingEnvironment.typeUtils.isAssignable(elementType, classMirror) || elementType == classMirror)
    }
}

/**
 * Whether the specified element is assignable to the [className] parameter
 */
fun TypeElement?.isSubclass(processingEnvironment: ProcessingEnvironment
                            = manager.processingEnvironment, className: ClassName)
        = isSubclass(processingEnvironment, className.toString())

/**
 * Whether the specified element is assignable to the [fqTn] parameter
 */
fun TypeElement?.isSubclass(processingEnvironment: ProcessingEnvironment, fqTn: String): Boolean {
    val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
    if (typeElement == null) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: $fqTn ensure that the visibility of the class is not private.")
        return false
    } else {
        val classMirror = typeElement.asType()
        return classMirror != null && this != null && this.asType() != null && processingEnvironment.typeUtils.isSubtype(this.asType(), classMirror)
    }
}

fun fromTypeMirror(typeMirror: TypeMirror, processorManager: ProcessorManager): ClassName? {
    val element = getTypeElement(typeMirror)
    return if (element != null) {
        ClassName.get(element)
    } else {
        ElementUtility.getClassName(typeMirror.toString(), processorManager)
    }
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
    val manager = manager
    var typeElement: TypeElement? = typeMirror.toTypeElement(manager)
    if (typeElement == null) {
        val el = manager.typeUtils.asElement(typeMirror)
        typeElement = if (el != null && el is TypeElement) el else null
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

inline fun <reified A : Annotation>
        Element.extractTypeNameFromAnnotation(invoker: (A) -> Unit): TypeName?
        = annotation<A>()?.let { a ->
    try {
        invoker(a)
    } catch (mte: MirroredTypeException) {
        return@let TypeName.get(mte.typeMirror)
    }
    return@let null
}

inline fun <reified A : Annotation>
        Element.extractTypeNameFromAnnotation(invoker: (A) -> Unit,
                                              exceptionHandler: (MirroredTypeException) -> Unit): TypeName?
        = annotation<A>()?.let { a ->
    return@let try {
        invoker(a)
        null
    } catch (mte: MirroredTypeException) {
        exceptionHandler(mte)
        TypeName.get(mte.typeMirror)
    }
}