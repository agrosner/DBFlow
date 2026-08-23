package com.dbflow5.processor.utils

import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.type.TypeMirror

val TypeMirror.typeName
    get() = TypeName.get(this)

val TypeMirror.kTypeName
    get() = typeName.toKTypeName()
