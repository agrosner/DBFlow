package com.dbflow5.processor.utils

import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

// element extensions

fun Element?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager) = this?.asType().toTypeElement(manager)


fun Element?.toTypeErasedElement(manager: ProcessorManager = ProcessorManager.manager) = this?.asType().erasure(manager).toTypeElement(manager)

val Element.simpleString
    get() = simpleName.toString()

// TypeMirror extensions

fun TypeMirror?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager): TypeElement? = manager.elements.getTypeElement(toString())

fun TypeMirror?.erasure(manager: ProcessorManager = ProcessorManager.manager): TypeMirror? = manager.typeUtils.erasure(this)


// TypeName

fun TypeName?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager): TypeElement? = manager.elements.getTypeElement(toString())

inline fun <reified T : Annotation> Element?.annotation() = this?.getAnnotation(T::class.java)

fun Element?.getPackage(manager: ProcessorManager = ProcessorManager.manager) = manager.elements.getPackageOf(this)

fun Element?.toClassName(): ClassName? = this?.let { ClassName.get(this as TypeElement) }