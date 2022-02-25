package com.dbflow5.processor.utils

import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

// element extensions

fun Element?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager) =
    this?.asType().toTypeElement(manager)

fun Element.toTypeErasedElement(manager: ProcessorManager = ProcessorManager.manager) =
    this.asType().erasure(manager).toTypeElement(manager)

@JvmName("toNullableTypeErasedElement")
fun Element?.toTypeErasedElement(manager: ProcessorManager = ProcessorManager.manager) =
    this?.asType().erasure(manager).toTypeElement(manager)

val Element.simpleString
    get() = simpleName.toString()

// TypeMirror extensions

fun TypeMirror.toTypeElement(manager: ProcessorManager = ProcessorManager.manager) =
    manager.typeUtils.asElement(this) as TypeElement

fun TypeMirror.toTypeElementOrNull(manager: ProcessorManager = ProcessorManager.manager) =
    manager.typeUtils.asElement(this) as TypeElement?

@JvmName("toNullableTypeElement")
fun TypeMirror?.toTypeElement(manager: ProcessorManager = ProcessorManager.manager): TypeElement? =
    manager.typeUtils.asElement(this) as TypeElement?

fun TypeMirror.erasure(manager: ProcessorManager = ProcessorManager.manager): TypeMirror =
    manager.typeUtils.erasure(this)

@JvmName("nullableErasure")
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
