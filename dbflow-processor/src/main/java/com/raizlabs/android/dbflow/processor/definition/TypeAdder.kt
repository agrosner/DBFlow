package com.raizlabs.android.dbflow.processor.definition

import com.squareup.javapoet.TypeSpec

interface TypeAdder {

    fun addToType(typeBuilder: TypeSpec.Builder)
}
