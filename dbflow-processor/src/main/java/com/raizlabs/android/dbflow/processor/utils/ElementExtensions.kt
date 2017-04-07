package com.raizlabs.android.dbflow.processor.utils

import com.raizlabs.android.dbflow.processor.ProcessorManager
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