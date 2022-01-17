package com.dbflow5.processor.utils

import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

// element extensions

fun Element?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager) =
    this?.asType().toTypeElement(manager)


fun Element?.toTypeErasedElement(manager: ProcessorManager = ProcessorManager.manager) =
    this?.asType().erasure(manager).toTypeElement(manager)

val Element.simpleString
    get() = simpleName.toString()

// TypeMirror extensions

fun TypeMirror?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager): TypeElement? =
    manager.elements.getTypeElement(toString())

fun TypeMirror?.erasure(manager: ProcessorManager = ProcessorManager.manager): TypeMirror? =
    manager.typeUtils.erasure(this)


// TypeName

fun TypeName?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager): TypeElement? =
    manager.elements.getTypeElement(toString())

@JvmName("nullableAnnotation")
inline fun <reified T : Annotation> Element?.annotation() = this?.getAnnotation(T::class.java)

inline fun <reified T : Annotation> Element.annotation() = this.getAnnotation(T::class.java)

fun Element?.getPackage(manager: ProcessorManager = ProcessorManager.manager) =
    manager.elements.getPackageOf(this)

fun Element?.toClassName(manager: ProcessorManager = ProcessorManager.manager): ClassName? {
    return when {
        this == null -> null
        this is TypeElement -> ClassName.get(this)
        else -> ElementUtility.getClassName(asType().toString(), manager)
    }
}

fun TypeName?.isOneOf(vararg kClass: KClass<*>): Boolean =
    this?.let { kClass.any { clazz -> TypeName.get(clazz.java) == this } }
        ?: false

fun TypeName.rawTypeName(): TypeName {
    if (this is ParameterizedTypeName) {
        return rawType
    }
    return this
}

fun TypeMirror.asStarProjectedType(): TypeMirror =
    ProcessorManager.manager.typeUtils
        .erasure(this)
