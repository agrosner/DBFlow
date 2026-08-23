package com.dbflow5.processor.utils

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

inline fun <reified A : Annotation>
    Element.extractTypeNameFromAnnotation(invoker: (A) -> Unit): TypeName? =
    annotation<A>()?.let { a ->
        try {
            invoker(a)
        } catch (mte: MirroredTypeException) {
            return@let TypeName.get(mte.typeMirror)
        }
        return@let null
    }

inline fun <reified A : Annotation>
    Element.extractClassNamesFromAnnotation(invoker: (A) -> Unit): List<TypeName>? =
    annotation<A>()?.let { a ->
        a.extractTypeMirrorsFromAnnotation(invoker = invoker)?.map { TypeName.get(it) }
    }

inline fun <reified A : Annotation>
    A.extractClassNamesFromAnnotation(invoker: (A) -> Unit): List<ClassName> =
    extractTypeMirrorsFromAnnotation(invoker = invoker)?.map {
        val toTypeElement = it.toTypeElement()!!
        val packageElement = toTypeElement.getPackage()!!
        ClassName.get(packageElement.toString(), toTypeElement.simpleString)
    }
        ?: listOf()

inline fun <reified A : Annotation>
    A.extractTypeNamesFromAnnotation(invoker: (A) -> Unit): List<TypeName> =
    extractTypeMirrorsFromAnnotation(invoker = invoker)?.map {
        TypeName.get(it)
    }
        ?: listOf()

inline fun <reified A : Annotation> A.extractTypeMirrorFromAnnotation(
    exceptionHandler: (MirroredTypeException) -> Unit = {},
    invoker: (A) -> Unit
)
    : TypeMirror? {
    var mirror: TypeMirror? = null
    try {
        invoker(this)
    } catch (mte: MirroredTypeException) {
        exceptionHandler(mte)
        mirror = mte.typeMirror
    }
    return mirror
}

inline fun <reified A : Annotation> A.extractTypeMirrorsFromAnnotation(
    exceptionHandler: (MirroredTypesException) -> Unit = {},
    invoker: (A) -> Unit
)
    : List<TypeMirror>? {
    try {
        invoker(this)
    } catch (mte: MirroredTypesException) {
        exceptionHandler(mte)
        return mte.typeMirrors
    }
    return null
}

inline fun <reified A : Annotation> A.extractTypeNameFromAnnotation(
    exceptionHandler: (MirroredTypeException) -> Unit = {},
    invoker: (A) -> Unit
): TypeName = TypeName.get(extractTypeMirrorFromAnnotation(exceptionHandler, invoker))

inline fun <reified A : Annotation> A.extractClassNameFromAnnotation(
    exceptionHandler: (MirroredTypeException) -> Unit = {},
    invoker: (A) -> Unit
): ClassName = extractTypeMirrorFromAnnotation(exceptionHandler, invoker)
    .let {
        val toTypeElement = it.toTypeElement()!!
        val packageElement = toTypeElement.getPackage()!!
        ClassName.get(packageElement.toString(), toTypeElement.simpleString)
    }